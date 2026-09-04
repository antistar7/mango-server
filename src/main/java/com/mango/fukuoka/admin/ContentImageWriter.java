package com.mango.fukuoka.admin;

import com.mango.fukuoka.content.FukuokaContent;
import com.mango.fukuoka.content.image.FukuokaContentImage;
import com.mango.fukuoka.content.image.FukuokaContentImageRepository;
import com.mango.fukuoka.content.image.FukuokaContentImageResponse;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 콘텐츠에 딸린 이미지를 요청 내용으로 통째로 교체한다.
 * URL이 비어 있는 행은 저장하지 않는다.
 */
final class ContentImageWriter {

    private ContentImageWriter() {
    }

    static List<FukuokaContentImageResponse> replace(
            FukuokaContentImageRepository repository,
            FukuokaContent content,
            List<FukuokaImageRequest> requests
    ) {
        repository.deleteByContentId(content.getId());
        repository.flush();

        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        AtomicInteger fallbackOrder = new AtomicInteger(0);

        List<FukuokaContentImage> toSave = requests.stream()
                .filter(request -> hasText(request.imageUrl()))
                .map(request -> FukuokaContentImage.create(
                        content,
                        request.imageUrl().trim(),
                        normalizeType(request.imageType()),
                        request.sortOrder() != null
                                ? request.sortOrder()
                                : fallbackOrder.getAndIncrement(),
                        blankToNull(request.caption())
                ))
                .toList();

        if (toSave.isEmpty()) {
            return List.of();
        }

        return repository.saveAll(toSave)
                .stream()
                .map(FukuokaContentImageResponse::from)
                .toList();
    }

    private static String normalizeType(String imageType) {
        if (imageType == null || imageType.isBlank()) {
            return "STORY";
        }

        return imageType.trim().toUpperCase();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String blankToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }
}
