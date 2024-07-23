package com.uninote.backend.entity;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "test_results")
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "test_results_seq")
    @SequenceGenerator(name = "test_results_seq", sequenceName = "test_results_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;


    @Column(name = "score", nullable = false)
    private BigDecimal score;

    

    
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

    
}
