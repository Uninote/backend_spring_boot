package com.uninote.backend.dto;

public class TrueFalseQuestionDTO extends QuestionDTO {
    private Boolean correctAnswer;

    

    public Boolean getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(Boolean correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
}
