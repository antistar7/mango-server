package com.mango.content;

import com.mango.exception.ContentNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContentService {

    private final ContentRepository contentRepository;

    public ContentService(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public List<ContentResponse> getContents() {
        return contentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ContentResponse getContent(Long id) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ContentNotFoundException(id));

        return toResponse(content);
    }

    public ContentResponse createContent(ContentRequest request) {

        Content content = new Content(
                request.getKorean(),
                request.getJapanese(),
                request.getDescription()
        );

        Content saved = contentRepository.save(content);

        return toResponse(saved);
    }

    public ContentResponse updateContent(Long id, ContentRequest request) {

        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ContentNotFoundException(id));

        content.update(
                request.getKorean(),
                request.getJapanese(),
                request.getDescription()
        );

        Content saved = contentRepository.save(content);

        return toResponse(saved);
    }

    public void deleteContent(Long id) {

        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ContentNotFoundException(id));

        contentRepository.delete(content);
    }

    private ContentResponse toResponse(Content content) {
        return new ContentResponse(
                content.getId(),
                content.getKorean(),
                content.getJapanese(),
                content.getDescription()
        );
    }
}