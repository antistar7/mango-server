package com.mango.content.generator;

import java.util.List;

public class ContentGenerationResponse {

    public String sourceLanguage;
    public String targetLanguage;

    public String sourceText;
    public String targetText;

    public String description;

    public Integer difficulty;

    public List<GeneratedExample> examples;
}