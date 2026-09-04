package com.mango.fukuoka.admin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException(
                    "JPG, PNG, WEBP 이미지만 업로드할 수 있습니다."
            );
        }

        String type = normalizeImageType(imageType);

        String extension = extension(file.getOriginalFilename());

        String filename =
                UUID.randomUUID() + extension;

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

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(
                        inputStream,
                        target,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

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

        throw new IllegalArgumentException(
                "imageType은 hero, thumbnail 또는 story여야 합니다."
        );
    }

    private String extension(String originalFilename) {
        if (originalFilename == null) {
            return ".jpg";
        }

        String lower =
                originalFilename.toLowerCase();

        if (lower.endsWith(".png")) {
            return ".png";
        }

        if (lower.endsWith(".webp")) {
            return ".webp";
        }

        return ".jpg";
    }
}
