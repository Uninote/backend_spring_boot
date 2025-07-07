package com.uninote.backend.entity;



import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "USER_FILTER_SEARCHES" )
public class UserFilterSearch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  
    @Column(name = "SEARCH_ID")
    private Long searchId;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Column(name = "UNIVERSITY_ID")
    private Long universityId;

    @Column(name = "DEPARTMENT_ID")
    private Long departmentId;

    @Column(name = "SEMESTER")
    private Integer semester;

    @Column(name = "COURSE_ID")
    private Long courseId;

    @Column(name = "SEARCHED_AT")
    private LocalDateTime searchedAt;

    @Column(name = "SESSION_ID")
    private Long sessionId;


    public UserFilterSearch() {
    }

    public Long getSearchId() {
        return searchId;
    }

    public void setSearchId(Long searchId) {
        this.searchId = searchId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUniversityId() {
        return universityId;
    }

    public void setUniversityId(Long universityId) {
        this.universityId = universityId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public LocalDateTime getSearchedAt() {
        return searchedAt;
    }

    public void setSearchedAt(LocalDateTime searchedAt) {
        this.searchedAt = searchedAt;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }
}
