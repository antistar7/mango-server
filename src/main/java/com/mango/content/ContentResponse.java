package com.mango.content;

public class ContentResponse {

    private final Long id;
    private final String korean;
    private final String japanese;
    private final String description;

    public ContentResponse(
            Long id,
            String korean,
            String japanese,
            String description
    ) {
        this.id = id;
        this.korean = korean;
        this.japanese = japanese;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

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