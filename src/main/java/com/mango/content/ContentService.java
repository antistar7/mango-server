package com.mango.content;

import com.mango.category.SubCategory;
import com.mango.category.SubCategoryRepository;
import com.mango.exception.ContentNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContentService {

    private final ContentRepository contentRepository;
    private final SubCategoryRepository subCategoryRepository;

    public ContentService(
            ContentRepository contentRepository,
            SubCategoryRepository subCategoryRepository
    ) {
        this.contentRepository = contentRepository;
        this.subCategoryRepository = subCategoryRepository;
    }

    public List<ContentResponse> getContents() {
        return contentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ContentResponse getContent(Long id) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ContentNotFoundException(id));

        return toResponse(content);
    }

    public ContentResponse createContent(ContentRequest request) {

        SubCategory subCategory = subCategoryRepository.findById(
                request.getSubCategoryId()
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "존재하지 않는 subCategoryId입니다: "
                                + request.getSubCategoryId()
                )
        );

        Content content = new Content(
                request.getKorean(),
                request.getJapanese(),
                request.getDescription(),
                subCategory
        );

        Content saved = contentRepository.save(content);

        return toResponse(saved);
    }

    public ContentResponse updateContent(
            Long id,
            ContentRequest request
    ) {

        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ContentNotFoundException(id));

        SubCategory subCategory = subCategoryRepository.findById(
                request.getSubCategoryId()
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "존재하지 않는 subCategoryId입니다: "
                                + request.getSubCategoryId()
                )
        );

        content.update(
                request.getKorean(),
                request.getJapanese(),
                request.getDescription(),
                subCategory
        );

        Content saved = contentRepository.save(content);

        return toResponse(saved);
    }

    public void deleteContent(Long id) {

        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ContentNotFoundException(id));

        contentRepository.delete(content);
    }

    private ContentResponse toResponse(Content content) {

        SubCategory subCategory = content.getSubCategory();

        return new ContentResponse(
                content.getId(),
                content.getKorean(),
                content.getJapanese(),
                content.getDescription(),

                subCategory.getCategory().getId(),
                subCategory.getCategory().getName(),

                subCategory.getId(),
                subCategory.getName()
        );
    }
}