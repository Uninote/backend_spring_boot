package com.uninote.backend.dto;

import java.util.List;

public class MultipleChoiceQuestionDTO extends QuestionDTO{

    private Long id;
    private Long questionId;
    private int correctChoiceLabel;
    private List<ChoiceDTO> choices;
    private String imageUrl;
   
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public int getCorrectChoiceLabel() {
        return correctChoiceLabel;
    }

    public void setCorrectChoiceLabel(int correctChoice) {
        this.correctChoiceLabel = correctChoice;
    }

    public List<ChoiceDTO> getChoices() {
        return choices;
    }

    public void setChoices(List<ChoiceDTO> choices) {
        this.choices = choices;
    }

    public String getImageUrl()  {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl= imageUrl;
    }
    
}
