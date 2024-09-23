package com.uninote.backend.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

@Entity
@Table(name = "courses", schema = "ADMIN")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "course_seq")
    @SequenceGenerator(name = "course_seq", sequenceName = "seq_course_id", allocationSize = 1)
    @Column(name = "course_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;


    @Column(name = "course_code", nullable = false)
    private String code;

    @Column(name="semester",nullable = false)
    private int semester;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CourseName> courseNames = new HashSet<>();
    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setSemester(int sem) {
        this.semester = sem;
    }

    public int getmSemester() {
        return this.semester;
    }

    public Set<CourseName> getCourseNames() {
        return courseNames;
    }

    public void setCourseNames(Set<CourseName> courseNames) {
        this.courseNames = courseNames;
    }
}
