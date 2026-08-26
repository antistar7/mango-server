package com.mango.study;

import java.time.LocalDateTime;

public class StudyHistoryResponse {

    private final Long id;
    private final Long contentId;
    private final String result;
    private final LocalDateTime studiedAt;

    public StudyHistoryResponse(
            Long id,
            Long contentId,
            String result,
            LocalDateTime studiedAt
    ) {
        this.id = id;
        this.contentId = contentId;
        this.result = result;
        this.studiedAt = studiedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getContentId() {
        return contentId;
    }

    public String getResult() {
        return result;
    }

    public LocalDateTime getStudiedAt() {
        return studiedAt;
    }
}