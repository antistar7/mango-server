package com.mango.study;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://161.34.67.76",
        "https://mango-love.com"
})
@RequestMapping("/api/v1/study-histories")
public class StudyHistoryController {

    private final StudyHistoryService studyHistoryService;

    public StudyHistoryController(
            StudyHistoryService studyHistoryService
    ) {
        this.studyHistoryService = studyHistoryService;
    }

    @PostMapping
    public ResponseEntity<Void> saveHistory(
            @Valid @RequestBody StudyHistoryRequest request
    ) {
        studyHistoryService.saveHistory(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<StudyHistoryResponse>> getHistories() {
        return ResponseEntity.ok(studyHistoryService.getHistories());
    }
}