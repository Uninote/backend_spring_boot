package com.uninote.backend.dto;

import java.util.List;
import java.util.Map;

public class QuestionnaireQuestionDTO {
    private String questionId;
    private String questionText;
    private String questionType; // TEXT, MULTIPLE_CHOICE, SINGLE_CHOICE, RATING, SCALE, BOOLEAN, DATE, FILE_UPLOAD
    private Integer order;
    private Boolean isRequired;
    private Boolean isVisible;
    private Map<String, Object> options;
    private List<QuestionnaireChoiceDTO> choices;
    private Map<String, Object> validation;
    private Map<String, Object> conditionalLogic;
    private Map<String, Object> styling;
    private String helpText;
    private String placeholder;

    // Constructors
    public QuestionnaireQuestionDTO() {}

    public QuestionnaireQuestionDTO(String questionId, String questionText, String questionType, 
                                   Integer order, Boolean isRequired, Boolean isVisible,
                                   Map<String, Object> options, List<QuestionnaireChoiceDTO> choices,
                                   Map<String, Object> validation, Map<String, Object> conditionalLogic,
                                   Map<String, Object> styling, String helpText, String placeholder) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.questionType = questionType;
        this.order = order;
        this.isRequired = isRequired;
        this.isVisible = isVisible;
        this.options = options;
        this.choices = choices;
        this.validation = validation;
        this.conditionalLogic = conditionalLogic;
        this.styling = styling;
        this.helpText = helpText;
        this.placeholder = placeholder;
    }

    // Getters and Setters
    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Boolean getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Boolean isRequired) {
        this.isRequired = isRequired;
    }

    public Boolean getIsVisible() {
        return isVisible;
    }

    public void setIsVisible(Boolean isVisible) {
        this.isVisible = isVisible;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }

    public List<QuestionnaireChoiceDTO> getChoices() {
        return choices;
    }

    public void setChoices(List<QuestionnaireChoiceDTO> choices) {
        this.choices = choices;
    }

    public Map<String, Object> getValidation() {
        return validation;
    }

    public void setValidation(Map<String, Object> validation) {
        this.validation = validation;
    }

    public Map<String, Object> getConditionalLogic() {
        return conditionalLogic;
    }

    public void setConditionalLogic(Map<String, Object> conditionalLogic) {
        this.conditionalLogic = conditionalLogic;
    }

    public Map<String, Object> getStyling() {
        return styling;
    }

    public void setStyling(Map<String, Object> styling) {
        this.styling = styling;
    }

    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }
} 