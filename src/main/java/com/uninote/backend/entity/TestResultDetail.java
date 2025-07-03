package com.uninote.backend.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "test_result_details" )
public class TestResultDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "test_result_details_seq")
    @SequenceGenerator(name = "test_result_details_seq", sequenceName = "test_result_details_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "test_result_id", nullable = false)
    private TestResult testResult;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;


    @Column(name = "correct", nullable = false)
    private boolean isCorrect;

    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TestResult getTestResult() {
        return testResult;
    }

    public void setTestResult(TestResult testResult) {
        this.testResult = testResult;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }


    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }
}
