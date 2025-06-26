package com.uninote.backend.dto;

import java.util.Map;

public class QuestionnaireChoiceDTO {
    private String choiceId;
    private String choiceText;
    private String choiceValue;
    private Integer order;
    private Boolean isCorrect;
    private Map<String, Object> metadata;
    private Map<String, Object> styling;

    // Constructors
    public QuestionnaireChoiceDTO() {}

    public QuestionnaireChoiceDTO(String choiceId, String choiceText, String choiceValue, 
                                 Integer order, Boolean isCorrect, Map<String, Object> metadata,
                                 Map<String, Object> styling) {
        this.choiceId = choiceId;
        this.choiceText = choiceText;
        this.choiceValue = choiceValue;
        this.order = order;
        this.isCorrect = isCorrect;
        this.metadata = metadata;
        this.styling = styling;
    }

    // Getters and Setters
    public String getChoiceId() {
        return choiceId;
    }

    public void setChoiceId(String choiceId) {
        this.choiceId = choiceId;
    }

    public String getChoiceText() {
        return choiceText;
    }

    public void setChoiceText(String choiceText) {
        this.choiceText = choiceText;
    }

    public String getChoiceValue() {
        return choiceValue;
    }

    public void setChoiceValue(String choiceValue) {
        this.choiceValue = choiceValue;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public Map<String, Object> getStyling() {
        return styling;
    }

    public void setStyling(Map<String, Object> styling) {
        this.styling = styling;
    }
} 