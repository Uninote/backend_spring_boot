package com.uninote.backend.entity;

import javax.persistence.*;

@Entity
@Table(name = "choices", schema = "ADMIN")
public class Choice {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "choices_seq")
    @SequenceGenerator(name = "choices_seq", sequenceName = "choices_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "multiple_choice_id", nullable = false)
    private MultipleChoiceQuestion multipleChoiceQuestion;

    @Column(name = "choice_text", nullable = false, length = 500)
    private String choiceText;

    @Column(name = "choice_label", nullable = false)
    private int choiceLabel;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MultipleChoiceQuestion getMultipleChoiceQuestion() {
        return multipleChoiceQuestion;
    }

    public void setMultipleChoiceQuestion(MultipleChoiceQuestion multipleChoiceQuestion) {
        this.multipleChoiceQuestion = multipleChoiceQuestion;
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
