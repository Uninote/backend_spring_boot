package com.uninote.backend.entity;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CourseNameId implements Serializable {

    private Long courseId;
    private Long languageId;

    // Default constructor
    public CourseNameId() {}

    // Parameterized constructor
    public CourseNameId(Long courseId, Long languageId) {
        this.courseId = courseId;
        this.languageId = languageId;
    }

    // Getters and setters
    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Long languageId) {
        this.languageId = languageId;
    }

    // Overriding equals method
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CourseNameId that = (CourseNameId) o;
        return Objects.equals(courseId, that.courseId) &&
               Objects.equals(languageId, that.languageId);
    }

    // Overriding hashCode method
    @Override
    public int hashCode() {
        return Objects.hash(courseId, languageId);
    }
}
