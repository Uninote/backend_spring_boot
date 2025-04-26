package com.uninote.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.util.RawValue;
import java.sql.Timestamp;

public abstract class ResourceDTO {
    @JsonProperty("id")
    protected Long id;
    
    @JsonProperty("title")
    protected String title;
    
    @JsonProperty("createdAt")
    protected Timestamp createdAt;
    
    @JsonProperty("summary")
    protected String summary;
    
    @JsonProperty("content")
    protected String content;
    
    @JsonProperty("type")
    protected String type;
    
    @JsonProperty("chapters")
    @JsonRawValue
    protected String chapters;
    
    @JsonProperty("flashcards")
    @JsonRawValue
    protected String flashcards;
    
    @JsonProperty("quizzes")
    @JsonRawValue
    protected String quizzes;
    
    @JsonProperty("relations")
    @JsonRawValue
    protected String relations;

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
    
    public String getRelations() {
        return relations;
    }

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
        this.chapters = (chapters != null && isValidJson(chapters)) ? chapters : "[]";
    }

    public void setFlashcards(String flashcards) {
        this.flashcards = (flashcards != null && isValidJson(flashcards)) ? flashcards : "[]";
    }

    public void setQuizzes(String quizzes) {
        this.quizzes = (quizzes != null && isValidJson(quizzes)) ? quizzes : "[]";
    }
    
    public void setRelations(String relations) {
        this.relations = (relations != null && isValidJson(relations)) ? relations : "[]";
    }
    
    
    private boolean isValidJson(String json) {
        try {
            if (json == null || json.trim().isEmpty()) {
                return false;
            }
            
            return (json.trim().startsWith("[") && json.trim().endsWith("]")) || 
                   (json.trim().startsWith("{") && json.trim().endsWith("}"));
        } catch (Exception e) {
            return false;
        }
    }
}