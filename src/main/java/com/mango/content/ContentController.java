package com.mango.content;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@RestController
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://161.34.67.76",
        "https://mango-love.com"
}   )
@RequestMapping("/api/v1/contents")
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping
    public List<ContentResponse> getContents() {
        return contentService.getContents();
    }

    @GetMapping("/{id}")
    public ContentResponse getContent(@PathVariable Long id) {
        return contentService.getContent(id);
    }

    @PostMapping
    public ContentResponse createContent(
            @Valid @RequestBody ContentRequest request
    ) {
        return contentService.createContent(request);
    }

    @PutMapping("/{id}")
    public ContentResponse updateContent(
            @PathVariable Long id,
            @Valid @RequestBody ContentRequest request
    ) {
        return contentService.updateContent(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteContent(@PathVariable Long id) {
        contentService.deleteContent(id);
    }
}