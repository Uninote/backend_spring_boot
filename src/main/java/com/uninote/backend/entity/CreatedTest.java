package com.uninote.backend.entity;

import javax.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "created_tests", schema = "ADMIN")
public class CreatedTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @Column(name = "TEST_ID", nullable = false)
    private Long testId;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Column(name = "CREATION_DATE", nullable = false)
    private LocalDateTime creationDate;


    @Column(name = "CLOSING_TIME", nullable = true)  
    private LocalDateTime closingTime;

    
    @Column(name = "SESSION_ID", nullable = true)
    private Long sessionId;
    @Column(name = "TYPE_ID", nullable = false)
    private Long typeId;

    
    @Column(name = "COURSE_ID", nullable = true)  
    private Long courseId;

    public CreatedTest() {
        
    }

    
    @PrePersist
    protected void onCreate() {
        this.creationDate = LocalDateTime.now(); 
    }

    public Long getTestId() {
        return testId;
    }

    public void setTestId(Long testId) {
        this.testId = testId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long course) {
        this.courseId = course;
    }

    public LocalDateTime getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(LocalDateTime closingTime) {
        this.closingTime = closingTime;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }
}
