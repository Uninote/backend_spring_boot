package com.uninote.backend.entity;

import javax.persistence.*;

@Entity
@Table(name = "true_false_questions")
public class TrueFalseQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tfq_seq")
    @SequenceGenerator(name = "tfq_seq", sequenceName = "tfq_seq", allocationSize = 1)
    @Column(name = "tfq_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "correct_answer", nullable = false)
    private Boolean correctAnswer;

    

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

    public Boolean getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(Boolean correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
}

