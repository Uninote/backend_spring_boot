package com.uninote.backend.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


public class QuestionReportId implements Serializable {

    @Column(name = "QUESTION_ID", nullable = false, precision = 38)
    private Long questionId;

    @Column(name = "USER_ID", nullable = false, precision = 38)
    private Long userId;

    @Column(name = "REPORT_DATE", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date reportDate;

    
    public QuestionReportId() {}

    public QuestionReportId(Long questionId, Long userId, Date reportDate) {
        this.questionId = questionId;
        this.userId = userId;
        this.reportDate = reportDate;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestionReportId that = (QuestionReportId) o;
        return Objects.equals(questionId, that.questionId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(reportDate, that.reportDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionId, userId, reportDate);
    }
}
