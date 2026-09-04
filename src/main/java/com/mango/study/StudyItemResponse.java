package com.mango.study;

import com.mango.content.ContentExampleResponse;

import java.util.List;

/**
 * 학습 화면이 소비하는 단일 학습 단위.
 *
 * 학습 콘텐츠(mango DB)와 여행 표현(mango_fukuoka DB)은 서로 다른
 * 영속성 단위에 있어 ID가 겹칠 수 있다. 화면에서의 식별자는 key를 사용하고,
 * contentId는 학습 이력을 남길 수 있는 LEARNING 항목에만 채워진다.
 */
public record StudyItemResponse(
        String key,
        String source,
        Long contentId,

        String sourceLanguage,
        String targetLanguage,

        String sourceText,
        String targetText,

        String reading,
        String description,

        String categoryName,
        String subCategoryName,

        Long travelContentId,
        String travelContentTitle,
        String cityName,

        List<ContentExampleResponse> examples
) {
}
