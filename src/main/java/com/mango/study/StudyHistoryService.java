package com.mango.study;

import com.mango.content.Content;
import com.mango.content.ContentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudyHistoryService {

    private final StudyHistoryRepository studyHistoryRepository;
    private final ContentRepository contentRepository;

    public StudyHistoryService(
            StudyHistoryRepository studyHistoryRepository,
            ContentRepository contentRepository
    ) {
        this.studyHistoryRepository = studyHistoryRepository;
        this.contentRepository = contentRepository;
    }

    @Transactional
    public void saveHistory(StudyHistoryRequest request) {

        Content content = contentRepository.findById(request.getContentId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "콘텐츠를 찾을 수 없습니다: " + request.getContentId()
                        )
                );

        StudyHistory history =
                new StudyHistory(content, request.getResult());

        studyHistoryRepository.save(history);
    }

    @Transactional(readOnly = true)
    public List<StudyHistoryResponse> getHistories() {
        return studyHistoryRepository.findAllByOrderByStudiedAtDesc()
                .stream()
                .map(history -> new StudyHistoryResponse(
                        history.getId(),
                        history.getContent().getId(),
                        history.getResult(),
                        history.getStudiedAt()
                ))
                .toList();
    }
}