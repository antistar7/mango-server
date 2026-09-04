package com.mango.fukuoka.geocoding;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/geocoding")
public class GeocodingController {

    private final GeocodingService geocodingService;

    public GeocodingController(
            GeocodingService geocodingService
    ) {
        this.geocodingService = geocodingService;
    }

    @GetMapping
    public GeocodingService.GeocodingResponse search(
            @RequestParam(required = false) String placeName,
            @RequestParam(required = false) String address
    ) {
        return geocodingService.search(
                placeName,
                address
        );
    }
}
