package com.uninote.backend.dto;

public class TrueFalseQuestionDTO extends QuestionDTO {
    private Boolean correctAnswer;
    private String imageUrl;

    

    public Boolean getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(Boolean correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getImageUrl()  {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl= imageUrl;
    }
}
