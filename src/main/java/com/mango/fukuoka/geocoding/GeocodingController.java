package com.mango.fukuoka.geocoding;

import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String citySlug,
            @RequestParam(required = false) String cityName
    ) {
        return geocodingService.search(
                placeName,
                address,
                citySlug,
                cityName
        );
    }

    @GetMapping("/candidates")
    public List<GeocodingService.GeocodingCandidate> candidates(
            @RequestParam String query,
            @RequestParam(required = false) String citySlug,
            @RequestParam(required = false) String cityName
    ) {
        return geocodingService.searchCandidates(
                query,
                citySlug,
                cityName
        );
    }

    @PostMapping("/candidates")
    public List<GeocodingService.GeocodingCandidate> candidatesPost(
            @RequestBody CandidateRequest request
    ) {
        return geocodingService.searchCandidates(
                request.query(),
                request.citySlug(),
                request.cityName(),
                request.area()
        );
    }

    @PostMapping("/locations")
    public List<GeocodingService.GeocodingCandidate> locationsPost(
            @RequestBody CandidateRequest request
    ) {
        return geocodingService.searchLocationCandidates(
                request.query(),
                request.citySlug(),
                request.cityName()
        );
    }

    public record CandidateRequest(
            String query,
            String citySlug,
            String cityName,
            String area
    ) {
    }
}
