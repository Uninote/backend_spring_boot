package com.uninote.backend.entity;

import javax.persistence.*;

@Entity
@Table(name = "course_names")
public class CourseName {

    @EmbeddedId
    private CourseNameId id;

    @ManyToOne
    @MapsId("courseId")
    @JoinColumn(name = "course_id", insertable = false, updatable = false)
    private Course course;

    @ManyToOne
    @MapsId("languageId")
    @JoinColumn(name = "language_id", insertable = false, updatable = false)
    private Language language;

    @Column(name = "course_name", nullable = false)
    private String name;

    

    // Constructors
    public CourseName() {}

    public CourseName(CourseNameId id, Course course, Language language, String name, String fullName) {
        this.id = id;
        this.course = course;
        this.language = language;
        this.name = name;
    }

    // Getters and setters
    public CourseNameId getId(){
        return id;
    } 

    public void setId(CourseNameId id){
        this.id =id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
