package com.uninote.backend.entity;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "test_results" )
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "test_results_seq")
    @SequenceGenerator(name = "test_results_seq", sequenceName = "test_results_seq", allocationSize = 1)
    private Long id;

    @OneToOne
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;


    @Column(name = "score", nullable = false)
    private BigDecimal score;

    @OneToMany(mappedBy = "testResult")
    private List<TestResultDetail> testResultDetails;
    

    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Test getTest() {
        return test;
    }

    public void setTest(Test test) {
        this.test = test;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public List<TestResultDetail> getTestResultDetails() {
        return testResultDetails;
    }

    public void setTestResultDetails(List<TestResultDetail> testResultDetails) {
        this.testResultDetails = testResultDetails;
    }
}
