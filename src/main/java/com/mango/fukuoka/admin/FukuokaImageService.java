package com.mango.fukuoka.admin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class FukuokaImageService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final Path imageRoot;

    public FukuokaImageService(
            @Value("${mango.image-root:/var/www/mango-images}")
            String imageRoot
    ) {
        this.imageRoot = Paths.get(imageRoot)
                .toAbsolutePath()
                .normalize();
    }

    public String upload(
            MultipartFile file,
            String imageType
    ) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "이미지 파일이 없습니다."
            );
        }

        return store(
                readBytes(file),
                file.getContentType(),
                file.getOriginalFilename(),
                imageType
        );
    }

    public String uploadJson(
            String imageType,
            String filename,
            String contentType,
            String data
    ) {
        if (data == null || data.isBlank()) {
            throw new IllegalArgumentException(
                    "이미지 데이터가 없습니다."
            );
        }

        String payload = data.trim();
        String resolvedType = contentType;

        int comma = payload.indexOf(',');
        if (payload.startsWith("data:") && comma > 0) {
            String meta = payload.substring(5, comma);
            int slash = meta.indexOf(';');
            if (slash > 0 && resolvedType == null) {
                resolvedType = meta.substring(0, slash);
            }
            payload = payload.substring(comma + 1);
        }

        byte[] bytes;
        try {
            bytes = java.util.Base64.getDecoder().decode(payload);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "이미지 데이터가 올바르지 않습니다."
            );
        }

        if (bytes.length == 0) {
            throw new IllegalArgumentException(
                    "이미지 파일이 없습니다."
            );
        }

        return store(bytes, resolvedType, filename, imageType);
    }

    private String store(
            byte[] bytes,
            String contentType,
            String originalFilename,
            String imageType
    ) {
        String resolvedContentType =
                contentType == null || contentType.isBlank()
                        ? "image/jpeg"
                        : contentType;

        if (!ALLOWED_TYPES.contains(resolvedContentType)) {
            throw new IllegalArgumentException(
                    "JPG, PNG, WEBP 이미지만 업로드할 수 있습니다."
            );
        }

        String type = normalizeImageType(imageType);
        String extension = extension(originalFilename, resolvedContentType);
        String filename = UUID.randomUUID() + extension;

        Path directory = imageRoot
                .resolve("fukuoka")
                .resolve(type);

        try {
            Files.createDirectories(directory);

            Path target = directory
                    .resolve(filename)
                    .normalize();

            if (!target.startsWith(directory)) {
                throw new IllegalArgumentException(
                        "잘못된 파일 경로입니다."
                );
            }

            Files.write(target, bytes);

            return "/images/fukuoka/"
                    + type
                    + "/"
                    + filename;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "이미지 저장에 실패했습니다.",
                    e
            );
        }
    }

    private static byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "이미지 저장에 실패했습니다.",
                    e
            );
        }
    }

    private String normalizeImageType(String imageType) {
        if ("hero".equalsIgnoreCase(imageType)) {
            return "hero";
        }

        if ("thumbnail".equalsIgnoreCase(imageType)) {
            return "thumbnail";
        }

        if ("story".equalsIgnoreCase(imageType)) {
            return "story";
        }

        if ("place".equalsIgnoreCase(imageType)) {
            return "place";
        }

        if ("city".equalsIgnoreCase(imageType)) {
            return "city";
        }

        if ("landing".equalsIgnoreCase(imageType)) {
            return "landing";
        }

        throw new IllegalArgumentException(
                "imageType은 hero, thumbnail, story, place, city 또는 landing이어야 합니다."
        );
    }

    private String extension(
            String originalFilename,
            String contentType
    ) {
        if (originalFilename != null) {
            String lower = originalFilename.toLowerCase();

            if (lower.endsWith(".png")) {
                return ".png";
            }

            if (lower.endsWith(".webp")) {
                return ".webp";
            }

            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                return ".jpg";
            }
        }

        if ("image/png".equals(contentType)) {
            return ".png";
        }

        if ("image/webp".equals(contentType)) {
            return ".webp";
        }

        return ".jpg";
    }
}
