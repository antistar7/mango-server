package com.mango.content.generator;

import com.mango.category.SubCategory;
import com.mango.category.SubCategoryRepository;
import com.mango.content.Content;
import com.mango.content.ContentExample;
import com.mango.content.ContentRepository;
import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.StructuredResponseCreateParams;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ContentGeneratorService {

    private static final int MAX_GENERATION_ATTEMPTS = 3;

    private final OpenAIClient openAIClient;
    private final ContentRepository contentRepository;
    private final SubCategoryRepository subCategoryRepository;
    private static final Logger log =
            LoggerFactory.getLogger(ContentGeneratorService.class);

    public ContentGeneratorService(
            OpenAIClient openAIClient,
            ContentRepository contentRepository,
            SubCategoryRepository subCategoryRepository
    ) {
        this.openAIClient = openAIClient;
        this.contentRepository = contentRepository;
        this.subCategoryRepository = subCategoryRepository;
    }

    @Transactional
    public ContentGenerationResponse generate(
            ContentGenerationRequest request
    ) {

        // 1. SubCategory 확인
        SubCategory subCategory =
                subCategoryRepository.findById(request.getSubCategoryId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "존재하지 않는 하위 카테고리입니다: "
                                                + request.getSubCategoryId()
                                )
                        );

        // 2. AI 콘텐츠 생성
        ContentGenerationResponse response = null;

        for (int attempt = 1; attempt <= MAX_GENERATION_ATTEMPTS; attempt++) {

            response = generateByAI(request);

            log.debug(
                    "[콘텐츠 생성] {}/{} 생성 완료",
                    attempt,
                    MAX_GENERATION_ATTEMPTS
            );

            // 3. 중복 확인
            boolean duplicated =
                    contentRepository
                            .existsBySourceLanguageAndTargetLanguageAndSourceText(
                                    response.sourceLanguage,
                                    response.targetLanguage,
                                    response.sourceText
                            );

            if (!duplicated) {
                log.debug("[콘텐츠 생성] 신규 콘텐츠 확인");
                break;
            }

            log.debug("[콘텐츠 생성] 중복 콘텐츠 → 재생성");

            if (attempt == MAX_GENERATION_ATTEMPTS) {
                throw new IllegalStateException(
                        "중복되지 않는 콘텐츠를 생성하지 못했습니다."
                );
            }
        }

        // 4. 다음 sort_order 결정
        Integer maxSortOrder =
                contentRepository.findMaxSortOrderBySubCategoryId(
                        request.getSubCategoryId()
                );

        int sortOrder = maxSortOrder + 1;

        // 5. Content 저장
        Content content = new Content(
                response.sourceLanguage,
                response.targetLanguage,
                response.sourceText,
                response.targetText,
                response.description,
                response.difficulty,
                sortOrder,
                subCategory
        );

        // 6. 예문 저장
        if (response.examples != null) {

            int exampleSortOrder = 1;

            for (GeneratedExample example : response.examples) {

                ContentExample contentExample =
                        new ContentExample(
                                content,
                                example.speaker,
                                example.sourceText,
                                example.targetText,
                                exampleSortOrder++
                        );

                content.getExamples().add(contentExample);
            }
        }

        // 7. Content + Examples 저장
        contentRepository.save(content);

        log.debug(
                "[콘텐츠 생성] 저장 완료 contentId={}",
                content.getId()
        );

        return response;
    }

    private ContentGenerationResponse generateByAI(
            ContentGenerationRequest request
    ) {

        String prompt = """
        You are a professional language-learning content creator.

        Create ONE natural and useful language-learning expression.

        Source language: %s
        Target language: %s
        Difficulty: %d

        Requirements:

        1. The source expression must be natural for native speakers.
        2. The target expression must NOT be a literal translation if
           that would sound unnatural.
        3. Preserve the actual meaning and conversational nuance.
        4. Prefer expressions that people really use in everyday conversation.
        5. Avoid textbook-like unnatural sentences.
        6. The description must be written in the SOURCE language.
        7. The description must explain when and how the expression is used.
        8. Create at least 2 realistic dialogue examples.
        9. Every example sourceText MUST be written in the SOURCE language.
        10. Every example targetText MUST be written in the TARGET language.
        11. Examples must be natural native-level expressions.
        12. Do not use English unless English is the source language or target language.
        13. Do not include explanations inside sourceText or targetText.
        14. Do not invent facts or cultural claims.
        15. speaker must contain only "A" or "B".
        16. sourceText MUST NOT contain speaker labels such as "A:", "B:", "A：", or "B：".
        17. targetText MUST NOT contain speaker labels such as "A:", "B:", "A：", or "B：".
        18. The speaker field and the text fields are separate. Do not repeat the speaker inside sourceText or targetText.        

        Return only the requested structured data.
        """.formatted(
                request.getSourceLanguage(),
                request.getTargetLanguage(),
                request.getDifficulty()
        );

        StructuredResponseCreateParams<ContentGenerationResponse> params =
                ResponseCreateParams.builder()
                        .input(prompt)
                        .text(ContentGenerationResponse.class)
                        .model(ChatModel.GPT_4O)
                        .build();

        return openAIClient
                .responses()
                .create(params)
                .output()
                .stream()
                .flatMap(item -> item.message().stream())
                .flatMap(message -> message.content().stream())
                .flatMap(content -> content.outputText().stream())
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("AI 응답이 없습니다.")
                );
    }
}