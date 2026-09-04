package com.mango.fukuoka.content.expression;

import com.mango.fukuoka.content.FukuokaContent;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "japanese_expression")
public class JapaneseExpression {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private FukuokaContent content;

    @Column(nullable = false, length = 500)
    private String expression;

    @Column(length = 500)
    private String translation;

    @Column(length = 500)
    private String reading;

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected JapaneseExpression() {
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public FukuokaContent getContent() {
        return content;
    }

    public String getExpression() {
        return expression;
    }

    public String getTranslation() {
        return translation;
    }

    public String getReading() {
        return reading;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public String getNote() {
        return note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void update(
            String expression,
            String translation,
            String reading,
            String audioUrl,
            String note
    ) {
        this.expression = expression;
        this.translation = translation;
        this.reading = reading;
        this.audioUrl = audioUrl;
        this.note = note;
    }

    public static JapaneseExpression create(
            FukuokaContent content,
            String expression,
            String translation,
            String reading,
            String audioUrl,
            String note
    ) {
        JapaneseExpression created = new JapaneseExpression();

        created.content = content;
        created.expression = expression;
        created.translation = translation;
        created.reading = reading;
        created.audioUrl = audioUrl;
        created.note = note;

        return created;
    }
}
