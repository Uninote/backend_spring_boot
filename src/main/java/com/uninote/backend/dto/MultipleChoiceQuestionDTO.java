package com.uninote.backend.dto;

import java.util.List;

public class MultipleChoiceQuestionDTO {

    private Long id;
    private Long questionId;
    private Long correctChoiceId;
    private List<ChoiceDTO> choices;

   
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

    public Long getCorrectChoiceId() {
        return correctChoiceId;
    }

    public void setCorrectChoiceId(Long correctChoiceId) {
        this.correctChoiceId = correctChoiceId;
    }

    public List<ChoiceDTO> getChoices() {
        return choices;
    }

    public void setChoices(List<ChoiceDTO> choices) {
        this.choices = choices;
    }
}
