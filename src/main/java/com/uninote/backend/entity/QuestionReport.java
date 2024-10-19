package com.uninote.backend.entity;


import javax.persistence.*;
import java.util.Date;


@Entity
@Table(name = "Question_Report", schema = "ADMIN")
public class QuestionReport {

    @EmbeddedId
    private QuestionReportId id;




    @Column(name = "STATUS", nullable = false, length = 1)
    private Integer status;


    public QuestionReport() {
        this.id = new QuestionReportId(); 
    }

    
    @PrePersist
    protected void onCreate() {
        this.status = 0; 
        this.id.setReportDate(new Date()); 
    }
    public QuestionReportId getId() {
        return id;
    }

    public void setId(QuestionReportId id) {
        this.id = id;
    }
    
    

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
