package com.mango.study;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class StudyHistoryRequest {

    @NotNull
    private Long contentId;

    @NotBlank
    private String result;

    public Long getContentId() {
        return contentId;
    }

    public String getResult() {
        return result;
    }
}