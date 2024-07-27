package com.uninote.backend.dto;

import java.util.List;

public class AddChoicesRequestDTO {
    private List<ChoiceDTO> choices;

    public List<ChoiceDTO> getChoices() {
        return choices;
    }

    public void setChoices(List<ChoiceDTO> choices) {
        this.choices = choices;
    }
}
