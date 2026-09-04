package com.mango.study;

import com.mango.category.SubCategory;
import com.mango.content.Content;
import com.mango.content.ContentExampleResponse;
import com.mango.content.ContentRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * mango DB의 학습 콘텐츠를 학습 단위로 변환한다.
 */
@Component
public class LearningStudyItemProvider {

    private final ContentRepository contentRepository;

    public LearningStudyItemProvider(
            ContentRepository contentRepository
    ) {
        this.contentRepository = contentRepository;
    }

    @Transactional(
            transactionManager = "transactionManager",
            readOnly = true
    )
    public List<StudyItemResponse> findAll() {
        return contentRepository.findAllForStudy()
                .stream()
                .map(this::toStudyItem)
                .toList();
    }

    private StudyItemResponse toStudyItem(
            Content content
    ) {
        SubCategory subCategory = content.getSubCategory();

        List<ContentExampleResponse> examples =
                content.getExamples()
                        .stream()
                        .map(example -> new ContentExampleResponse(
                                example.getId(),
                                example.getSpeaker(),
                                example.getSourceText(),
                                example.getTargetText(),
                                example.getSortOrder()
                        ))
                        .toList();

        return new StudyItemResponse(
                StudyItemSource.LEARNING.keyOf(content.getId()),
                StudyItemSource.LEARNING.name(),
                content.getId(),

                content.getSourceLanguage(),
                content.getTargetLanguage(),

                content.getSourceText(),
                content.getTargetText(),

                null,
                content.getDescription(),

                subCategory.getCategory().getName(),
                subCategory.getName(),

                null,
                null,
                null,

                examples
        );
    }
}
