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

@Service
public class GeocodingService {

    private static final Logger log =
            LoggerFactory.getLogger(GeocodingService.class);

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeocodingResponse search(
            String placeName,
            String address,
            String citySlug,
            String cityName
    ) {
        String query = buildQuery(placeName, address, citySlug, cityName);

        try {
            JsonNode results = searchNominatim(query, citySlug, cityName);

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

    private JsonNode searchNominatim(
            String query,
            String citySlug,
            String cityName
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
            url.append("&bounded=1");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url.toString()))
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
}
