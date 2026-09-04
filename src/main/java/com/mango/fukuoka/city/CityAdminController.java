package com.mango.fukuoka.city;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/cities")
public class CityAdminController {

    private final CityAdminService service;

    public CityAdminController(CityAdminService service) {
        this.service = service;
    }

    @GetMapping
    public List<CityAdminResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public CityAdminResponse findById(
            @PathVariable Long id
    ) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CityAdminResponse create(
            @RequestBody CityAdminRequest request
    ) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public CityAdminResponse update(
            @PathVariable Long id,
            @RequestBody CityAdminRequest request
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
