package com.mango.fukuoka.site;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/landing")
public class LandingAdminController {

    private final LandingService landingService;

    public LandingAdminController(LandingService landingService) {
        this.landingService = landingService;
    }

    @GetMapping
    public LandingService.LandingResponse get() {
        return landingService.get();
    }

    @PutMapping
    public LandingService.LandingResponse update(
            @RequestBody LandingService.LandingRequest request
    ) {
        return landingService.update(request);
    }
}
