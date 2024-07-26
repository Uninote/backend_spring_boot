package com.uninote.backend.dto;

public class ChoiceDTO {

    private Long id;
    private String choiceText;
    private int choiceLabel;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChoiceText() {
        return choiceText;
    }

    public void setChoiceText(String choiceText) {
        this.choiceText = choiceText;
    }

    public int getChoiceLabel() {
        return choiceLabel;
    }

    public void setChoiceLabel(int choiceLabel) {
        this.choiceLabel = choiceLabel;
    }
}
