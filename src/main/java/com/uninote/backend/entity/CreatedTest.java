package com.uninote.backend.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "created_tests")
public class CreatedTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @Column(name = "TEST_ID", nullable = false)
    private Long testId;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Column(name = "CREATION_DATE", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date creationDate;

    @Column(name = "TYPE_ID", nullable = false)
    private Long typeId;

    public CreatedTest() {
        
    }

    
    @PrePersist
    protected void onCreate() {
        this.creationDate = new Date(); 
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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }
}
