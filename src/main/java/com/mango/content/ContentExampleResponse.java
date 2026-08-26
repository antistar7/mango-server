package com.mango.content;

public record ContentExampleResponse(
        Long id,
        String speaker,
        String sourceText,
        String targetText,
        Integer sortOrder
) {
}