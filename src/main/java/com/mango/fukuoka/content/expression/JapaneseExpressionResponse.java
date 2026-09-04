package com.mango.fukuoka.content.expression;

public record JapaneseExpressionResponse(
        Long id,
        String expression,
        String translation,
        String reading,
        String audioUrl,
        String note
) {
    public static JapaneseExpressionResponse from(JapaneseExpression expression) {
        return new JapaneseExpressionResponse(
                expression.getId(),
                expression.getExpression(),
                expression.getTranslation(),
                expression.getReading(),
                expression.getAudioUrl(),
                expression.getNote()
        );
    }
}
