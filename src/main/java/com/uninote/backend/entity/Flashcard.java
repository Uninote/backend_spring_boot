package com.uninote.backend.entity;

import javax.persistence.*;

@Entity
@Table(name = "flashcards", schema = "ADMIN")
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "flashcard_seq")
    @SequenceGenerator(name = "flashcard_seq", sequenceName = "flashcard_seq", allocationSize = 1)
    @Column(name = "flashcard_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Lob
    @Column(name = "answer", nullable = false)
    private String answer;

    public Flashcard(){}

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

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
