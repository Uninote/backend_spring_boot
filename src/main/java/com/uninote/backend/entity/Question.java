package com.uninote.backend.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "questions" )
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "question_seq")
    @SequenceGenerator(name = "question_seq", sequenceName = "question_seq", allocationSize = 1)
    @Column(name = "question_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne
    @JoinColumn(name = "question_type_id")
    private QuestionType questionType;

    @Lob
    @Column(name = "question_text", nullable = false)
    private String questionText;

    @Column(name = "is_difficult", nullable = false)
    private Boolean isDifficult;

    @OneToMany(mappedBy = "question")
    private List<TestResultDetail> testResultDetails;

    public Question(){}

   public Boolean getIsDifficult(){
    return isDifficult;
   }

   public void setIsDifficult(Boolean isDifficult){
    this.isDifficult = isDifficult;
   }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public List<TestResultDetail> getTestResultDetails() {
        return testResultDetails;
    }

    public void setTestResultDetails(List<TestResultDetail> testResultDetails) {
        this.testResultDetails = testResultDetails;
    }
}
