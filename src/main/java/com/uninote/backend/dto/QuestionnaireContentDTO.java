package com.uninote.backend.dto;

import java.util.List;
import java.util.Map;

public class QuestionnaireContentDTO {
    private String questionnaireId;
    private String title;
    private String description;
    private String questionnaireType;
    private List<QuestionnaireQuestionDTO> questions;
    private Map<String, Object> settings;
    private Map<String, Object> styling;
    private Map<String, Object> logic;
    private String version;

    // Constructors
    public QuestionnaireContentDTO() {}

    public QuestionnaireContentDTO(String questionnaireId, String title, String description, 
                                  String questionnaireType, List<QuestionnaireQuestionDTO> questions,
                                  Map<String, Object> settings, Map<String, Object> styling,
                                  Map<String, Object> logic, String version) {
        this.questionnaireId = questionnaireId;
        this.title = title;
        this.description = description;
        this.questionnaireType = questionnaireType;
        this.questions = questions;
        this.settings = settings;
        this.styling = styling;
        this.logic = logic;
        this.version = version;
    }

    // Getters and Setters
    public String getQuestionnaireId() {
        return questionnaireId;
    }

    public void setQuestionnaireId(String questionnaireId) {
        this.questionnaireId = questionnaireId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getQuestionnaireType() {
        return questionnaireType;
    }

    public void setQuestionnaireType(String questionnaireType) {
        this.questionnaireType = questionnaireType;
    }

    public List<QuestionnaireQuestionDTO> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionnaireQuestionDTO> questions) {
        this.questions = questions;
    }

    public Map<String, Object> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, Object> settings) {
        this.settings = settings;
    }

    public Map<String, Object> getStyling() {
        return styling;
    }

    public void setStyling(Map<String, Object> styling) {
        this.styling = styling;
    }

    public Map<String, Object> getLogic() {
        return logic;
    }

    public void setLogic(Map<String, Object> logic) {
        this.logic = logic;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
} 