package com.mango.fukuoka.admin;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/fukuoka/contents")
public class FukuokaAdminController {

    private final FukuokaAdminService service;

    public FukuokaAdminController(
            FukuokaAdminService service
    ) {
        this.service = service;
    }

    @GetMapping
    public List<FukuokaContentResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public FukuokaContentResponse findById(
            @PathVariable Long id
    ) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FukuokaContentResponse create(
            @RequestBody FukuokaContentRequest request
    ) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public FukuokaContentResponse update(
            @PathVariable Long id,
            @RequestBody FukuokaContentRequest request
    ) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id
    ) {
        service.delete(id);
    }
}
