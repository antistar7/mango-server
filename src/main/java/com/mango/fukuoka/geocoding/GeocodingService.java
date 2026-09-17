package com.mango.fukuoka.geocoding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GeocodingService {

    private static final Logger log =
            LoggerFactory.getLogger(GeocodingService.class);

    private static final Set<String> GENERIC_SHOP_NAMES = Set.of(
            "食堂",
            "レストラン",
            "カフェ",
            "ラーメン",
            "居酒屋",
            "定食",
            "弁当",
            "restaurant",
            "cafe",
            "shop",
            "store"
    );
    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(8);
    private static final long GEOCODING_JP_GAP_MS = 11_000;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ConcurrentHashMap<String, GeocodingCandidate> landmarkCache =
            new ConcurrentHashMap<>();
    private final Object geocodingJpLock = new Object();
    private long lastGeocodingJpAt;

    public GeocodingResponse search(
            String placeName,
            String address,
            String citySlug,
            String cityName
    ) {
        String query = buildQuery(placeName, address, citySlug, cityName);

        try {
            JsonNode results = searchNominatim(query, citySlug, cityName, true);

            JsonNode best = findBestResult(results);

            if (best == null) {
                throw new IllegalArgumentException(
                        "지역의 좌표를 찾을 수 없습니다."
                );
            }

            log.debug(
                    "[GEOCODING SELECTED] {} / {} / {}",
                    best.path("lat").asText(),
                    best.path("lon").asText(),
                    best.path("display_name").asText()
            );

            return new GeocodingResponse(
                    best.path("lat").asText(),
                    best.path("lon").asText(),
                    best.path("display_name").asText()
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "좌표 검색이 중단되었습니다.",
                    e
            );

        } catch (IllegalArgumentException e) {
            throw e;

        } catch (Exception e) {
            throw new IllegalStateException(
                    "주소 좌표 검색에 실패했습니다.",
                    e
            );
        }
    }

    public List<GeocodingCandidate> searchCandidates(
            String query,
            String citySlug,
            String cityName
    ) {
        return searchCandidates(query, citySlug, cityName, null);
    }

    public List<GeocodingCandidate> searchCandidates(
            String query,
            String citySlug,
            String cityName,
            String area
    ) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("검색어를 입력해주세요.");
        }

        String shopName = query.trim();
        String cityJa = cityLabelJa(citySlug, cityName);
        BranchHint branch = parseBranch(shopName, area);

        try {
            LinkedHashMap<String, RankedCandidate> unique = new LinkedHashMap<>();
            boolean triedLandmark = false;

            if (looksLikeUniqueShopName(branch.core())) {
                mergeShopHits(
                        unique,
                        landmarkShopHits(shopName, branch, cityJa),
                        shopName,
                        branch,
                        citySlug,
                        cityName
                );
                triedLandmark = true;
            }

            if (unique.isEmpty()) {
                for (String variant : shopQueryVariants(shopName, branch)) {
                    mergeShopHits(
                            unique,
                            nominatimCandidates(variant + " " + cityJa, null, null, false),
                            shopName,
                            branch,
                            citySlug,
                            cityName
                    );
                    mergeShopHits(
                            unique,
                            nominatimCandidates(variant, null, null, false),
                            shopName,
                            branch,
                            citySlug,
                            cityName
                    );
                }
            }

            if (unique.isEmpty() && !triedLandmark) {
                mergeShopHits(
                        unique,
                        landmarkShopHits(shopName, branch, cityJa),
                        shopName,
                        branch,
                        citySlug,
                        cityName
                );
            }

            List<GeocodingCandidate> chosen = unique.values().stream()
                    .sorted(Comparator.comparingInt(RankedCandidate::rank))
                    .map(RankedCandidate::candidate)
                    .limit(15)
                    .toList();

            log.info(
                    "[GEOCODING SHOP] query={} area={} city={} hits={}",
                    shopName,
                    branch.area(),
                    cityJa,
                    chosen.size()
            );

            return chosen;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("좌표 검색이 중단되었습니다.", e);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("주소 좌표 검색에 실패했습니다.", e);
        }
    }

    public List<GeocodingCandidate> searchLocationCandidates(
            String query,
            String citySlug,
            String cityName
    ) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("검색어를 입력해주세요.");
        }

        String location = query.trim();
        String cityJa = cityLabelJa(citySlug, cityName);

        try {
            LinkedHashMap<String, RankedCandidate> unique = new LinkedHashMap<>();
            mergeLocationHits(
                    unique,
                    gsiHits(location),
                    citySlug,
                    cityName
            );
            mergeLocationHits(
                    unique,
                    gsiHits(location + " " + cityJa),
                    citySlug,
                    cityName
            );
            mergeLocationHits(
                    unique,
                    nominatimCandidates(location + " " + cityJa, null, null, false),
                    citySlug,
                    cityName
            );
            mergeLocationHits(
                    unique,
                    nominatimCandidates(location, null, null, false),
                    citySlug,
                    cityName
            );

            return unique.values().stream()
                    .sorted(Comparator.comparingInt(RankedCandidate::rank))
                    .map(RankedCandidate::candidate)
                    .limit(15)
                    .toList();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("좌표 검색이 중단되었습니다.", e);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("주소 좌표 검색에 실패했습니다.", e);
        }
    }

    private List<ShopHit> nominatimCandidates(
            String query,
            String citySlug,
            String cityName,
            boolean bounded
    ) throws Exception {
        JsonNode results = searchNominatim(query, citySlug, cityName, bounded);
        List<ShopHit> hits = new ArrayList<>();

        for (JsonNode result : results) {
            GeocodingCandidate candidate = fromNominatim(result);
            if (candidate != null) {
                hits.add(new ShopHit(
                        candidate,
                        result.path("category").asText(),
                        result.path("type").asText()
                ));
            }
        }

        return hits;
    }

    private static GeocodingCandidate fromNominatim(JsonNode result) {
        String latitude = result.path("lat").asText();
        String longitude = result.path("lon").asText();
        if (latitude.isBlank() || longitude.isBlank()) {
            return null;
        }

        String name = result.path("name").asText();
        String displayName = result.path("display_name").asText();

        return new GeocodingCandidate(
                latitude,
                longitude,
                name.isBlank() ? displayName : name,
                displayName
        );
    }

    private static void mergeShopHits(
            LinkedHashMap<String, RankedCandidate> unique,
            List<ShopHit> incoming,
            String shopName,
            BranchHint branch,
            String citySlug,
            String cityName
    ) {
        for (ShopHit hit : incoming) {
            GeocodingCandidate candidate = hit.candidate();
            if (!nearCity(
                    candidate.latitude(),
                    candidate.longitude(),
                    citySlug,
                    cityName
            )) {
                continue;
            }

            if (!nameLooksLikeShop(candidate, shopName, branch)) {
                continue;
            }

            String key = roundCoord(candidate.latitude())
                    + ","
                    + roundCoord(candidate.longitude());
            int rank = rankShopHit(hit, shopName, branch);
            RankedCandidate existing = unique.get(key);
            if (existing == null || rank < existing.rank()) {
                unique.put(key, new RankedCandidate(candidate, rank));
            }
        }
    }

    private static void mergeLocationHits(
            LinkedHashMap<String, RankedCandidate> unique,
            List<ShopHit> incoming,
            String citySlug,
            String cityName
    ) {
        for (ShopHit hit : incoming) {
            GeocodingCandidate candidate = hit.candidate();
            if (!nearCity(
                    candidate.latitude(),
                    candidate.longitude(),
                    citySlug,
                    cityName
            )) {
                continue;
            }

            String key = roundCoord(candidate.latitude())
                    + ","
                    + roundCoord(candidate.longitude());
            int rank = isPoi(hit.category()) ? 0 : 2;
            RankedCandidate existing = unique.get(key);
            if (existing == null || rank < existing.rank()) {
                unique.put(key, new RankedCandidate(candidate, rank));
            }
        }
    }

    private static boolean nameLooksLikeShop(
            GeocodingCandidate candidate,
            String shopName,
            BranchHint branch
    ) {
        String query = normalizeShopText(shopName);
        String core = normalizeShopText(branch.core());
        String normalizedName = normalizeShopText(candidate.name());

        if (GENERIC_SHOP_NAMES.contains(normalizedName)
                && !normalizedName.equals(query)
                && !normalizedName.equals(core)) {
            return false;
        }

        if (normalizedName.equals(query) || normalizedName.equals(core)) {
            return true;
        }

        if (core.length() >= 3 && normalizedName.contains(core)) {
            return true;
        }

        if (core.length() >= 4
                && normalizedName.length() >= 4
                && core.contains(normalizedName)
                && !GENERIC_SHOP_NAMES.contains(normalizedName)) {
            return true;
        }

        String area = branch.area() == null ? "" : normalizeShopText(branch.area());
        List<String> required = distinctiveTokens(shopName, branch).stream()
                .filter(token -> !GENERIC_SHOP_NAMES.contains(token))
                .filter(token -> area.isBlank() || !token.equals(area))
                .toList();

        if (required.isEmpty()) {
            return false;
        }

        return required.stream().allMatch(normalizedName::contains);
    }

    private static int rankShopHit(ShopHit hit, String shopName, BranchHint branch) {
        String normalizedName = normalizeShopText(hit.candidate().name());
        String display = hit.candidate().displayName() == null
                ? ""
                : hit.candidate().displayName();
        String core = normalizeShopText(branch.core());
        int rank = 4;

        if (normalizedName.equals(core) || normalizedName.equals(normalizeShopText(shopName))) {
            rank = 0;
        } else if (normalizedName.contains(core) || core.contains(normalizedName)) {
            rank = 1;
        } else {
            rank = 3;
        }

        if (isPoi(hit.category())) {
            rank = Math.max(0, rank - 1);
        } else {
            rank += 2;
        }

        if (branch.area() != null && display.contains(branch.area())) {
            rank = Math.max(0, rank - 1);
        }

        return rank;
    }

    private static boolean isPoi(String category) {
        return "amenity".equals(category)
                || "shop".equals(category)
                || "tourism".equals(category)
                || "leisure".equals(category);
    }

    private static List<String> shopQueryVariants(String shopName, BranchHint branch) {
        LinkedHashSet<String> variants = new LinkedHashSet<>();
        variants.add(shopName);
        variants.add(branch.core());
        variants.add(normalizeReadable(branch.core()).replace("処", ""));
        variants.add(swapKana(branch.core()));

        String trailingKana = trailingKana(branch.core());
        if (trailingKana != null) {
            variants.add(trailingKana);
            if (branch.area() != null) {
                variants.add(trailingKana + " " + branch.area());
            }
        }

        if (branch.core().startsWith("$") && branch.core().length() > 1) {
            variants.add("S" + branch.core().substring(1));
            variants.add("s" + branch.core().substring(1));
        }

        if (branch.area() != null) {
            variants.add(branch.core() + " " + branch.area());
            variants.add(normalizeReadable(branch.core()) + " " + branch.area());
            if (branch.core().startsWith("$") && branch.core().length() > 1) {
                variants.add("S" + branch.core().substring(1) + " " + branch.area());
            }
        }

        variants.removeIf(item -> item == null || item.isBlank());
        return List.copyOf(variants);
    }

    private static BranchHint parseBranch(String shopName, String areaHint) {
        String cleaned = shopName
                .replaceAll("[（(][^）)]*[）)]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        String core = cleaned;
        String area = areaHint == null || areaHint.isBlank() ? null : areaHint.trim();

        String[] parts = cleaned.split(" ");
        if (parts.length >= 2 && parts[parts.length - 1].endsWith("店")) {
            String last = parts[parts.length - 1];
            String maybeArea = last.substring(0, last.length() - 1);
            if (!maybeArea.isBlank()) {
                area = maybeArea;
            }
            core = String.join(" ", java.util.Arrays.copyOf(parts, parts.length - 1)).trim();
        }

        return new BranchHint(core.isBlank() ? cleaned : core, area);
    }

    private List<ShopHit> landmarkShopHits(
            String shopName,
            BranchHint branch,
            String cityJa
    ) {
        List<ShopHit> hits = new ArrayList<>();

        if (looksLikeAddress(shopName)) {
            hits.addAll(gsiHits(shopName));
            if (!hits.isEmpty()) {
                return hits;
            }
        }

        for (String query : landmarkQueries(shopName, branch, cityJa)) {
            GeocodingCandidate candidate = fetchGeocodingJp(query);
            if (candidate != null) {
                hits.add(new ShopHit(candidate, "amenity", "restaurant"));
                break;
            }
        }

        return hits;
    }

    private static List<String> landmarkQueries(
            String shopName,
            BranchHint branch,
            String cityJa
    ) {
        LinkedHashSet<String> queries = new LinkedHashSet<>();
        boolean generic = hasGenericShopToken(shopName);

        if (generic) {
            queries.add(shopName + " " + cityJa);
            queries.add(shopName);
        } else {
            queries.add(shopName);
            if (cityJa != null && !shopName.contains(cityJa)) {
                queries.add(shopName + " " + cityJa);
            }
        }

        if (branch != null && !branch.core().equals(shopName)) {
            queries.add(branch.core());
        }

        queries.removeIf(item -> item == null || item.isBlank());
        return List.copyOf(queries);
    }

    private static boolean hasGenericShopToken(String shopName) {
        for (String part : shopName.split("\\s+")) {
            if (GENERIC_SHOP_NAMES.contains(normalizeShopText(part))) {
                return true;
            }
        }
        return false;
    }

    private static boolean looksLikeUniqueShopName(String value) {
        String compact = value == null ? "" : value.replaceAll("\\s+", "");
        if (compact.length() < 3) {
            return false;
        }
        if (GENERIC_SHOP_NAMES.contains(normalizeShopText(compact))) {
            return false;
        }

        for (int i = 0; i < compact.length(); i++) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(compact.charAt(i));
            if (block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                    || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                    || block == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS) {
                return false;
            }
        }

        return true;
    }

    private static boolean looksLikeAddress(String query) {
        boolean hasDigit = query.chars().anyMatch(Character::isDigit);
        return hasDigit && (
                query.contains("丁目")
                        || query.contains("番地")
                        || query.contains("区")
                        || query.contains("市")
                        || query.contains("-")
                        || query.contains("−")
                        || query.contains("ー")
        );
    }

    private List<ShopHit> gsiHits(String query) {
        List<ShopHit> hits = new ArrayList<>();
        if (query == null || query.isBlank()) {
            return hits;
        }

        try {
            String url = "https://msearch.gsi.go.jp/address-search/AddressSearch?q="
                    + URLEncoder.encode(query, StandardCharsets.UTF_8);
            JsonNode results = objectMapper.readTree(httpGet(url, "application/json"));
            if (!results.isArray()) {
                return hits;
            }

            for (JsonNode result : results) {
                JsonNode coordinates = result.path("geometry").path("coordinates");
                if (!coordinates.isArray() || coordinates.size() < 2) {
                    continue;
                }

                String longitude = coordinates.get(0).asText();
                String latitude = coordinates.get(1).asText();
                String title = result.path("properties").path("title").asText();
                if (latitude.isBlank() || longitude.isBlank() || title.isBlank()) {
                    continue;
                }

                hits.add(new ShopHit(
                        new GeocodingCandidate(latitude, longitude, title, title),
                        "place",
                        "address"
                ));
            }
        } catch (Exception e) {
            log.info("[GEOCODING GSI] query={} failed={}", query, e.getMessage());
        }

        return hits;
    }

    private GeocodingCandidate fetchGeocodingJp(String query) {
        GeocodingCandidate cached = landmarkCache.get(query);
        if (cached != null) {
            return cached;
        }

        try {
            throttleGeocodingJp();
            String url = "https://www.geocoding.jp/api/?q="
                    + URLEncoder.encode(query, StandardCharsets.UTF_8);
            GeocodingCandidate candidate = parseGeocodingJpXml(
                    httpGet(url, "application/xml")
            );
            if (candidate != null) {
                landmarkCache.put(query, candidate);
            }
            return candidate;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.info("[GEOCODING JP] interrupted query={}", query);
            return null;
        } catch (Exception e) {
            log.info("[GEOCODING JP] query={} failed={}", query, e.getMessage());
            return null;
        }
    }

    private void throttleGeocodingJp() throws InterruptedException {
        synchronized (geocodingJpLock) {
            long wait = GEOCODING_JP_GAP_MS - (System.currentTimeMillis() - lastGeocodingJpAt);
            if (lastGeocodingJpAt > 0 && wait > 0) {
                Thread.sleep(wait);
            }
            lastGeocodingJpAt = System.currentTimeMillis();
        }
    }

    static GeocodingCandidate parseGeocodingJpXml(String xml) {
        if (xml == null || !xml.contains("<result>")) {
            return null;
        }

        String lat = xmlTag(xml, "lat");
        String lng = xmlTag(xml, "lng");
        if (!hasCoordinate(lat) || !hasCoordinate(lng)) {
            return null;
        }

        String googleMaps = xmlTag(xml, "google_maps");
        String address = xmlTag(xml, "address");
        String display = !googleMaps.isBlank() ? googleMaps : address;
        if (display.isBlank()) {
            return null;
        }

        return new GeocodingCandidate(lat, lng, display, display);
    }

    private static boolean hasCoordinate(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            return Double.parseDouble(value) != 0.0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static String xmlTag(String xml, String tag) {
        Matcher matcher = Pattern.compile(
                "<" + Pattern.quote(tag) + ">(.*?)</" + Pattern.quote(tag) + ">",
                Pattern.DOTALL
        ).matcher(xml);
        if (matcher.find()) {
            return matcher.group(1).strip();
        }
        return "";
    }

    private String httpGet(String url, String accept) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(HTTP_TIMEOUT)
                .header("User-Agent", "MANGO/1.0 (https://mango-love.com)")
                .header("Accept", accept)
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("HTTP " + response.statusCode());
        }
        return response.body();
    }

    private static String swapKana(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (character >= 'ァ' && character <= 'ン') {
                out.append((char) (character - 0x60));
            } else if (character >= 'ぁ' && character <= 'ん') {
                out.append((char) (character + 0x60));
            } else {
                out.append(character);
            }
        }
        return out.toString();
    }

    private static String trailingKana(String value) {
        String compact = value.replace("処", "").replace("處", "");
        java.util.regex.Matcher matcher =
                java.util.regex.Pattern.compile("([ぁ-んァ-ンー]{2,})$").matcher(compact);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static String normalizeReadable(String value) {
        return value
                .replace("天麩羅", "天ぷら")
                .replace("天婦羅", "天ぷら")
                .replace("處", "処");
    }

    private static String normalizeShopText(String value) {
        if (value == null) {
            return "";
        }

        return normalizeReadable(value)
                .replace("$", "s")
                .replace("処", "")
                .replaceAll("[\\s・·.,''\"()（）【】]", "")
                .toLowerCase();
    }

    private static List<String> distinctiveTokens(String shopName, BranchHint branch) {
        LinkedHashSet<String> tokens = new LinkedHashSet<>();
        for (String part : (branch.core() + " " + shopName).split("\\s+")) {
            String token = normalizeShopText(part.replaceAll("店$", ""));
            if (token.isBlank() || token.equals("福岡") || token.equals("大阪") || token.equals("日本")) {
                continue;
            }
            tokens.add(token);
        }
        return List.copyOf(tokens);
    }

    private static boolean nearCity(
            String latitude,
            String longitude,
            String citySlug,
            String cityName
    ) {
        double[] center = cityCenter(citySlug, cityName);
        if (center == null) {
            return true;
        }

        try {
            double lat = Double.parseDouble(latitude);
            double lon = Double.parseDouble(longitude);
            return haversineKm(lat, lon, center[0], center[1]) <= 40;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    private static double haversineKm(
            double lat1,
            double lon1,
            double lat2,
            double lon2
    ) {
        double earthKm = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        return earthKm * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private static String roundCoord(String value) {
        try {
            return String.format(java.util.Locale.US, "%.5f", Double.parseDouble(value));
        } catch (NumberFormatException e) {
            return value;
        }
    }

    private static String cityLabelJa(String citySlug, String cityName) {
        if (isFukuoka(citySlug, cityName)) {
            return "福岡";
        }
        if (isOsaka(citySlug, cityName)) {
            return "大阪";
        }
        if (cityName != null && !cityName.isBlank()) {
            return cityName.trim();
        }
        return "日本";
    }

    private static double[] cityCenter(String citySlug, String cityName) {
        if (isFukuoka(citySlug, cityName)) {
            return new double[] {33.5902, 130.4017};
        }
        if (isOsaka(citySlug, cityName)) {
            return new double[] {34.6937, 135.5023};
        }
        return null;
    }

    private record RankedCandidate(GeocodingCandidate candidate, int rank) {
    }

    private record ShopHit(
            GeocodingCandidate candidate,
            String category,
            String type
    ) {
    }

    private record BranchHint(String core, String area) {
    }

    private JsonNode searchNominatim(
            String query,
            String citySlug,
            String cityName,
            boolean bounded
    ) throws Exception {

        String encodedQuery = URLEncoder.encode(
                query,
                StandardCharsets.UTF_8
        );

        StringBuilder url = new StringBuilder(
                "https://nominatim.openstreetmap.org/search"
                        + "?q=" + encodedQuery
                        + "&format=jsonv2"
                        + "&limit=10"
                        + "&countrycodes=jp"
                        + "&accept-language=ja"
        );

        /*
         * 좌표 검색도 도시별로 가둔다.
         * 후쿠오카 viewbox에 오사카를 넣으면 난바가 후쿠오카 좌표로 나온다.
         */
        String viewbox = viewboxFor(citySlug, cityName);
        if (viewbox != null) {
            url.append("&viewbox=").append(viewbox);
            if (bounded) {
                url.append("&bounded=1");
            }
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url.toString()))
                .timeout(HTTP_TIMEOUT)
                .header(
                        "User-Agent",
                        "MANGO/1.0 (https://mango-love.com)"
                )
                .header(
                        "Accept",
                        "application/json"
                )
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "Nominatim HTTP "
                            + response.statusCode()
            );
        }

        JsonNode results =
                objectMapper.readTree(response.body());

        if (!results.isArray() || results.isEmpty()) {
            return objectMapper.createArrayNode();
        }

        log.debug("[GEOCODING QUERY] {}", query);

        for (JsonNode result : results) {
            log.debug(
                    "[GEOCODING RESULT] {}, {} / {} / {} / {}",
                    result.path("lat").asText(),
                    result.path("lon").asText(),
                    result.path("category").asText(),
                    result.path("type").asText(),
                    result.path("display_name").asText()
            );
        }

        return results;
    }

    private JsonNode findBestResult(JsonNode results) {

        if (results == null || !results.isArray() || results.isEmpty()) {
            return null;
        }

        String[] preferredAddressTypes = {
                "quarter",
                "neighbourhood",
                "suburb",
                "city_district",
                "town",
                "village"
        };

        for (String addressType : preferredAddressTypes) {
            for (JsonNode result : results) {
                if (addressType.equals(
                        result.path("addresstype").asText()
                )) {
                    return result;
                }
            }
        }

        for (JsonNode result : results) {
            if ("park".equals(result.path("type").asText())) {
                return result;
            }
        }

        return results.get(0);
    }

    private String buildQuery(
            String placeName,
            String address,
            String citySlug,
            String cityName
    ) {
        StringBuilder query = new StringBuilder();

        if (placeName != null && !placeName.isBlank()) {
            query.append(placeName.trim());
        }

        if (address != null && !address.isBlank()) {
            if (!query.isEmpty()) {
                query.append(", ");
            }

            query.append(address.trim());
        }

        if (!query.isEmpty()) {
            query.append(", ");
        }

        query.append(citySuffix(citySlug, cityName));

        return query.toString();
    }

    private static String citySuffix(String citySlug, String cityName) {
        if (isFukuoka(citySlug, cityName)) {
            return "福岡市, 福岡県, 日本";
        }

        if (isOsaka(citySlug, cityName)) {
            return "大阪市, 大阪府, 日本";
        }

        if (cityName != null && !cityName.isBlank()) {
            return cityName.trim() + ", 日本";
        }

        return "日本";
    }

    private static String viewboxFor(String citySlug, String cityName) {
        if (isFukuoka(citySlug, cityName)) {
            return "130.35,33.65,130.45,33.55";
        }

        if (isOsaka(citySlug, cityName)) {
            return "135.43,34.75,135.58,34.60";
        }

        return null;
    }

    private static boolean isFukuoka(String citySlug, String cityName) {
        return containsAny(
                citySlug,
                cityName,
                "fukuoka",
                "福岡",
                "후쿠오카"
        );
    }

    private static boolean isOsaka(String citySlug, String cityName) {
        return containsAny(
                citySlug,
                cityName,
                "osaka",
                "大阪",
                "오사카"
        );
    }

    private static boolean containsAny(
            String citySlug,
            String cityName,
            String... tokens
    ) {
        String haystack = (
                (citySlug == null ? "" : citySlug)
                        + " "
                        + (cityName == null ? "" : cityName)
        ).toLowerCase();

        for (String token : tokens) {
            if (haystack.contains(token.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    public record GeocodingResponse(
            String latitude,
            String longitude,
            String displayName
    ) {
    }

    public record GeocodingCandidate(
            String latitude,
            String longitude,
            String name,
            String displayName
    ) {
    }
}
