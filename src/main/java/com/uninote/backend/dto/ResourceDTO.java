package com.uninote.backend.dto;

import java.sql.Timestamp;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.uninote.backend.utils.JsonUtils;

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
    protected JsonNode chapters;

    @JsonProperty("flashcards")
    protected JsonNode flashcards;

    @JsonProperty("quizzes")
    protected JsonNode quizzes;

    @JsonProperty("relations")
    protected JsonNode relations;



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

    public JsonNode getChapters() {
        return chapters;
    }

    public JsonNode getFlashcards() {
        return flashcards;
    }

    public JsonNode getQuizzes() {
        return quizzes;
    }

    public JsonNode getRelations() {
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
        this.chapters = parseJsonString(chapters);
    }

    public void setFlashcards(String flashcards) {
        this.flashcards = parseJsonString(flashcards);
    }

    public void setQuizzes(String quizzes) {
        this.quizzes = parseJsonString(quizzes);
    }

    public void setRelations(String relations) {
        this.relations = parseJsonString(relations);
    }

        /**
     * Parse a JSON string into a JsonNode, handling null/empty values and malformed JSON
     */
    private JsonNode parseJsonString(String jsonString) {
        return JsonUtils.parseJsonString(jsonString);
    }

    /**
     * Get chapters as a List of objects
     */
    public List<Object> getChaptersAsList() {
        return JsonUtils.jsonNodeToList(chapters);
    }

    /**
     * Get flashcards as a List of objects
     */
    public List<Object> getFlashcardsAsList() {
        return JsonUtils.jsonNodeToList(flashcards);
    }

    /**
     * Get quizzes as a List of objects
     */
    public List<Object> getQuizzesAsList() {
        return JsonUtils.jsonNodeToList(quizzes);
    }

    /**
     * Get relations as a List of objects
     */
    public List<Object> getRelationsAsList() {
        return JsonUtils.jsonNodeToList(relations);
    }

    /**
     * Set chapters from a List of objects
     */
    public void setChaptersFromList(List<?> chaptersList) {
        this.chapters = JsonUtils.listToJsonNode(chaptersList);
    }

    /**
     * Set flashcards from a List of objects
     */
    public void setFlashcardsFromList(List<?> flashcardsList) {
        this.flashcards = JsonUtils.listToJsonNode(flashcardsList);
    }

    /**
     * Set quizzes from a List of objects
     */
    public void setQuizzesFromList(List<?> quizzesList) {
        this.quizzes = JsonUtils.listToJsonNode(quizzesList);
    }

    /**
     * Set relations from a List of objects
     */
    public void setRelationsFromList(List<?> relationsList) {
        this.relations = JsonUtils.listToJsonNode(relationsList);
    }
}
