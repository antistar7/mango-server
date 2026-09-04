package com.mango.study;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/study")
public class StudyItemController {

    private final StudyItemService studyItemService;

    public StudyItemController(
            StudyItemService studyItemService
    ) {
        this.studyItemService = studyItemService;
    }

    /**
     * @param city 지정하면 해당 도시의 여행 표현만 포함한다.
     *             학습 콘텐츠는 도시와 무관하므로 항상 포함된다.
     */
    @GetMapping("/items")
    public List<StudyItemResponse> getItems(
            @RequestParam(required = false) String city
    ) {
        return studyItemService.findAll(city);
    }
}
