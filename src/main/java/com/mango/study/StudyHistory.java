package com.mango.study;

import com.mango.content.Content;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "study_histories")
public class StudyHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @Column(nullable = false, length = 20)
    private String result;

    @Column(name = "studied_at", nullable = false)
    private LocalDateTime studiedAt;

    protected StudyHistory() {
    }

    public StudyHistory(Content content, String result) {
        this.content = content;
        this.result = result;
        this.studiedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Content getContent() {
        return content;
    }

    public String getResult() {
        return result;
    }

    public LocalDateTime getStudiedAt() {
        return studiedAt;
    }
}