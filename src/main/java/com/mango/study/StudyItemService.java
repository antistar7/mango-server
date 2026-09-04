package com.mango.study;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 서로 다른 DB에 있는 두 학습 소스를 하나의 목록으로 합친다.
 *
 * 영속성 단위가 분리되어 있어 DB 조인이 불가능하므로 이 계층에서 합친다.
 * 각 소스 조회는 자신의 트랜잭션 매니저를 쓰는 provider가 담당하고,
 * 여기서는 트랜잭션을 열지 않는다.
 */
@Service
public class StudyItemService {

    private final TravelStudyItemProvider travelProvider;
    private final LearningStudyItemProvider learningProvider;

    public StudyItemService(
            TravelStudyItemProvider travelProvider,
            LearningStudyItemProvider learningProvider
    ) {
        this.travelProvider = travelProvider;
        this.learningProvider = learningProvider;
    }

    public List<StudyItemResponse> findAll(
            String citySlug
    ) {
        return interleave(
                travelProvider.findAll(citySlug),
                learningProvider.findAll()
        );
    }

    /**
     * 한쪽을 앞에 몰아두면 클라이언트가 하루치를 잘라낼 때 다른 쪽이 묻힌다.
     * 번갈아 배치해 두 소스가 함께 노출되도록 한다.
     */
    private static List<StudyItemResponse> interleave(
            List<StudyItemResponse> first,
            List<StudyItemResponse> second
    ) {
        List<StudyItemResponse> merged =
                new ArrayList<>(first.size() + second.size());

        int maxSize = Math.max(first.size(), second.size());

        for (int index = 0; index < maxSize; index++) {
            if (index < first.size()) {
                merged.add(first.get(index));
            }

            if (index < second.size()) {
                merged.add(second.get(index));
            }
        }

        return merged;
    }
}
