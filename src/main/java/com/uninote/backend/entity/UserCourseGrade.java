package com.uninote.backend.entity;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "user_course_grades")
public class UserCourseGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "grade", precision = 5, scale = 2)
    private Double grade;

    @Column(name = "date_assigned", nullable = false)
    private LocalDate dateAssigned = LocalDate.now();


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUser() {
        return userId;
    }

    public void setUserId(Long user) {
        this.userId = user;
    }

    public Long getCourse() {
        return courseId;
    }

    public void setCourseId(Long course) {
        this.courseId = course;
    }

    public Double getGrade() {
        return grade;
    }

    public void setGrade(Double grade) {
        this.grade = grade;
    }

    public LocalDate getDateAssigned() {
        return dateAssigned;
    }

    public void setDateAssigned(LocalDate dateAssigned) {
        this.dateAssigned = dateAssigned;
    }
}
