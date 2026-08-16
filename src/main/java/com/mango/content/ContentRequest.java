package com.mango.content;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ContentRequest {

    @NotBlank(message = "korean은 필수입니다.")
    @Size(max = 100, message = "korean은 100자 이하여야 합니다.")
    private String korean;

    @NotBlank(message = "japanese는 필수입니다.")
    @Size(max = 100, message = "japanese는 100자 이하여야 합니다.")
    private String japanese;

    @Size(max = 500, message = "description은 500자 이하여야 합니다.")
    private String description;

    public String getKorean() {
        return korean;
    }

    public String getJapanese() {
        return japanese;
    }

    public String getDescription() {
        return description;
    }
}