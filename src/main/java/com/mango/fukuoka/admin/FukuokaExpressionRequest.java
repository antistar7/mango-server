package com.mango.fukuoka.admin;

public record FukuokaExpressionRequest(
        String expression,
        String translation,
        String reading,
        String audioUrl,
        String note
) {
}
