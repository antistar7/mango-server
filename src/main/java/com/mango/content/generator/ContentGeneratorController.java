package com.mango.content.generator;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/content-generator")
public class ContentGeneratorController {

    private final ContentGeneratorService contentGeneratorService;

    public ContentGeneratorController(
            ContentGeneratorService contentGeneratorService
    ) {
        this.contentGeneratorService = contentGeneratorService;
    }

    @PostMapping("/generate")
    public ContentGenerationResponse generate(
            @RequestBody ContentGenerationRequest request
    ) {
        return contentGeneratorService.generate(request);
    }
}