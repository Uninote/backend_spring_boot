package com.uninote.backend.entity;

import java.util.List;
import javax.persistence.*;

@Entity
@Table(name = "multiple_choice_questions")
public class MultipleChoiceQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "multiple_choice_questions_seq")
    @SequenceGenerator(name = "multiple_choice_questions_seq", sequenceName = "multiple_choice_questions_seq", allocationSize = 1)
    @Column(name = "multiple_choice_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @OneToOne
    @JoinColumn(name = "correct_choice_id", referencedColumnName = "id")
    private Choice correctChoice;

    @OneToMany(mappedBy = "multipleChoiceQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Choice> choices;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public Choice getCorrectChoice() {
        return correctChoice;
    }

    public void setCorrectChoice(Choice correctChoice) {
        this.correctChoice = correctChoice;
    }

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }
}
