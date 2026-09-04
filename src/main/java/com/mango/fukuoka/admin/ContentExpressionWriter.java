package com.mango.fukuoka.admin;

import com.mango.fukuoka.content.FukuokaContent;
import com.mango.fukuoka.content.expression.JapaneseExpression;
import com.mango.fukuoka.content.expression.JapaneseExpressionRepository;
import com.mango.fukuoka.content.expression.JapaneseExpressionResponse;

import java.util.List;

/**
 * 콘텐츠에 딸린 일본어 표현을 요청 내용으로 통째로 교체한다.
 * 표현 문장이 비어 있는 행은 저장 대상에서 제외한다.
 */
final class ContentExpressionWriter {

    private ContentExpressionWriter() {
    }

    static List<JapaneseExpressionResponse> replace(
            JapaneseExpressionRepository repository,
            FukuokaContent content,
            List<FukuokaExpressionRequest> requests
    ) {
        repository.deleteByContentId(content.getId());
        repository.flush();

        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        List<JapaneseExpression> toSave = requests.stream()
                .filter(request -> hasText(request.expression()))
                .map(request -> JapaneseExpression.create(
                        content,
                        request.expression().trim(),
                        blankToNull(request.translation()),
                        blankToNull(request.reading()),
                        blankToNull(request.audioUrl()),
                        blankToNull(request.note())
                ))
                .toList();

        if (toSave.isEmpty()) {
            return List.of();
        }

        return repository.saveAll(toSave)
                .stream()
                .map(JapaneseExpressionResponse::from)
                .toList();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String blankToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }
}
