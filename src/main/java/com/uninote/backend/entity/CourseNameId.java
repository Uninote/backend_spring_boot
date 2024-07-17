package com.uninote.backend.entity;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CourseNameId implements Serializable {

    private Long courseId;
    private Long languageId;

    
    public CourseNameId() {}

    
    public CourseNameId(Long courseId, Long languageId) {
        this.courseId = courseId;
        this.languageId = languageId;
    }

    
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

    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CourseNameId that = (CourseNameId) o;
        return Objects.equals(courseId, that.courseId) &&
               Objects.equals(languageId, that.languageId);
    }

    
    @Override
    public int hashCode() {
        return Objects.hash(courseId, languageId);
    }
}
