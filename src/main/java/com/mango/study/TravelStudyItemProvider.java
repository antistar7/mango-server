package com.mango.study;

import com.mango.fukuoka.content.FukuokaContent;
import com.mango.fukuoka.content.expression.JapaneseExpression;
import com.mango.fukuoka.content.expression.JapaneseExpressionRepository;
import com.mango.fukuoka.place.FukuokaPlace;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * mango_fukuoka DB의 여행 표현을 학습 단위로 변환한다.
 *
 * 여행 표현은 학습 콘텐츠와 다른 DB에 있어 study_histories FK를 걸 수 없다.
 * 그래서 contentId 없이 내려가고, 학습 이력도 남기지 않는다.
 */
@Component
public class TravelStudyItemProvider {

    private static final String CATEGORY_LABEL = "여행 표현";
    private static final String SOURCE_LANGUAGE = "ko";
    private static final String TARGET_LANGUAGE = "ja";

    private final JapaneseExpressionRepository expressionRepository;

    public TravelStudyItemProvider(
            JapaneseExpressionRepository expressionRepository
    ) {
        this.expressionRepository = expressionRepository;
    }

    @Transactional(
            transactionManager = "fukuokaTransactionManager",
            readOnly = true
    )
    public List<StudyItemResponse> findAll(
            String citySlug
    ) {
        List<JapaneseExpression> expressions =
                citySlug == null || citySlug.isBlank()
                        ? expressionRepository.findAllPublishedForStudy()
                        : expressionRepository
                                .findAllPublishedForStudyByCity(citySlug);

        return expressions.stream()
                .filter(TravelStudyItemProvider::isStudyable)
                .map(TravelStudyItemProvider::toStudyItem)
                .toList();
    }

    /**
     * 플래시카드는 번역을 질문으로 쓴다. 번역이 없으면 물어볼 것이 없어 제외한다.
     */
    private static boolean isStudyable(
            JapaneseExpression expression
    ) {
        return expression.getExpression() != null
                && !expression.getExpression().isBlank()
                && expression.getTranslation() != null
                && !expression.getTranslation().isBlank();
    }

    private static StudyItemResponse toStudyItem(
            JapaneseExpression expression
    ) {
        FukuokaContent content = expression.getContent();

        return new StudyItemResponse(
                StudyItemSource.TRAVEL.keyOf(expression.getId()),
                StudyItemSource.TRAVEL.name(),
                null,

                SOURCE_LANGUAGE,
                TARGET_LANGUAGE,

                expression.getTranslation(),
                expression.getExpression(),

                expression.getReading(),
                expression.getNote(),

                CATEGORY_LABEL,
                content.getTitle(),

                content.getId(),
                content.getTitle(),
                cityNameOf(content),

                List.of()
        );
    }

    private static String cityNameOf(
            FukuokaContent content
    ) {
        FukuokaPlace place = content.getPlace();

        if (place == null || place.getCity() == null) {
            return null;
        }

        return place.getCity().getName();
    }
}
