package com.uninote.backend.dto;

import java.sql.Timestamp;

public abstract class ResourceDTO {
    protected Long id;
    protected String title;
    protected Timestamp createdAt;
    protected String summary;
    protected String content;
    protected String type;
    protected String chapters;
    protected String flashcards;
    protected String quizzes;

    // Getters
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public String getSummary() {
        return summary;
    }

    public String getContent() {
        return content;
    }

    public String getType() {
        return type;
    }

    public String getChapters() {
        return chapters;
    }

    public String getFlashcards() {
        return flashcards;
    }

    public String getQuizzes() {
        return quizzes;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setChapters(String chapters) {
        this.chapters = chapters;
    }

    public void setFlashcards(String flashcards) {
        this.flashcards = flashcards;
    }

    public void setQuizzes(String quizzes) {
        this.quizzes = quizzes;
    }
}
