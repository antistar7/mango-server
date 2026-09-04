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
            String address
    ) {
        String query = buildQuery(placeName, address);

        try {
            JsonNode results = searchNominatim(query);

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

        } catch (Exception e) {
            throw new IllegalStateException(
                    "주소 좌표 검색에 실패했습니다.",
                    e
            );
        }
    }

    private JsonNode searchNominatim(String query)
            throws Exception {

        String encodedQuery = URLEncoder.encode(
                query,
                StandardCharsets.UTF_8
        );

        String url =
                "https://nominatim.openstreetmap.org/search"
                        + "?q=" + encodedQuery
                        + "&format=jsonv2"
                        + "&limit=10"
                        + "&countrycodes=jp"
                        + "&viewbox=130.35,33.65,130.45,33.55"
                        + "&bounded=1"
                        + "&accept-language=ja";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
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

        /*
         * 1. 지역 자체를 나타내는 결과를 최우선
         */
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

        /*
         * 2. 공원 등 명확한 장소 자체
         */
        for (JsonNode result : results) {
            if ("park".equals(result.path("type").asText())) {
                return result;
            }
        }

        /*
         * 3. 지역 타입이 없는 경우
         *    Nominatim의 첫 번째 검색 결과를 사용
         */
        return results.get(0);
    }

    private java.util.List<String> extractPlaceTokens(JsonNode result) {

        java.util.List<String> tokens =
                new java.util.ArrayList<>();

        JsonNode address = result.path("address");

        if (address.isObject()) {
            address.fields().forEachRemaining(entry -> {
                if (entry.getValue().isTextual()) {
                    tokens.add(entry.getValue().asText());
                }
            });
        }

        return tokens;
    }
    private String buildQuery(
            String placeName,
            String address
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

        query.append("福岡市, 福岡県, 日本");

        return query.toString();
    }

    public record GeocodingResponse(
            String latitude,
            String longitude,
            String displayName
    ) {
    }
}
