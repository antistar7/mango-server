package com.mango.content;

import jakarta.persistence.*;

@Entity
@Table(name = "contents")
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String korean;

    @Column(nullable = false, length = 100)
    private String japanese;

    @Column(length = 500)
    private String description;

    protected Content() {
    }

    public Content(String korean, String japanese, String description) {
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

    public void update(String korean, String japanese, String description) {
        this.korean = korean;
        this.japanese = japanese;
        this.description = description;
    }
}