package com.mango.fukuoka.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> upload(
            @RequestParam("file")
            MultipartFile file,

            @RequestParam("imageType")
            String imageType
    ) {
        return Map.of("url", imageService.upload(file, imageType));
    }

    /**
     * multipart가 프록시에서 깨지는 경우를 피하기 위해
     * base64 JSON으로도 받는다.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> uploadJson(
            @RequestBody ImageUploadJsonRequest request
    ) {
        return Map.of(
                "url",
                imageService.uploadJson(
                        request.imageType(),
                        request.filename(),
                        request.contentType(),
                        request.data()
                )
        );
    }

    public record ImageUploadJsonRequest(
            String imageType,
            String filename,
            String contentType,
            String data
    ) {
    }
}
