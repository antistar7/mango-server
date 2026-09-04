package com.mango.fukuoka.admin;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/fukuoka/images")
public class FukuokaImageController {

    private final FukuokaImageService imageService;

    public FukuokaImageController(
            FukuokaImageService imageService
    ) {
        this.imageService = imageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> upload(
            @RequestParam("file")
            MultipartFile file,

            @RequestParam("imageType")
            String imageType
    ) {
        String url =
                imageService.upload(file, imageType);

        return Map.of("url", url);
    }
}
