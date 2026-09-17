package com.mango.fukuoka.geocoding;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class GeocodingServiceTest {

    @Test
    void parseGeocodingJpXmlReadsShopAddress() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8" ?>
                <result>
                <version>1.2</version>
                <address>ソルリバ</address>
                <coordinate>
                <lat>33.582217</lat>
                <lng>130.403977</lng>
                </coordinate>
                <google_maps>福岡県福岡市中央区渡辺通２丁目３−３２ 三角市場内 博多酒場 ソルリバ</google_maps>
                </result>
                """;

        GeocodingService.GeocodingCandidate candidate =
                GeocodingService.parseGeocodingJpXml(xml);

        assertNotNull(candidate);
        assertEquals("33.582217", candidate.latitude());
        assertEquals("130.403977", candidate.longitude());
        assertEquals(
                "福岡県福岡市中央区渡辺通２丁目３−３２ 三角市場内 博多酒場 ソルリバ",
                candidate.displayName()
        );
    }

    @Test
    void parseGeocodingJpXmlIgnoresZeroCoordinate() {
        String xml = """
                <result>
                <address>ソルリバ 福岡</address>
                <coordinate>
                <lat>0</lat>
                <lng>0</lng>
                </coordinate>
                <google_maps></google_maps>
                </result>
                """;

        assertNull(GeocodingService.parseGeocodingJpXml(xml));
    }
}
