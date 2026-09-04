package com.mango.fukuoka.admin;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/cities/{citySlug}/contents")
public class CityAdminContentController {

    private final CityAdminContentService service;

    public CityAdminContentController(
            CityAdminContentService service
    ) {
        this.service = service;
    }

    @GetMapping
    public List<FukuokaContentResponse> findAll(
            @PathVariable String citySlug
    ) {
        return service.findAll(citySlug);
    }

    @GetMapping("/{id}")
    public FukuokaContentResponse findById(
            @PathVariable String citySlug,
            @PathVariable Long id
    ) {
        return service.findById(citySlug, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FukuokaContentResponse create(
            @PathVariable String citySlug,
            @RequestBody FukuokaContentRequest request
    ) {
        return service.create(citySlug, request);
    }

    @PutMapping("/{id}")
    public FukuokaContentResponse update(
            @PathVariable String citySlug,
            @PathVariable Long id,
            @RequestBody FukuokaContentRequest request
    ) {
        return service.update(citySlug, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable String citySlug,
            @PathVariable Long id
    ) {
        service.delete(citySlug, id);
    }
}
