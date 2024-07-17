package com.uninote.backend.entity;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Embeddable;

@Embeddable
public class UniversityNameId implements Serializable {
    private Long universityId;
    private Long languageId;

    // Default constructor
    public UniversityNameId() {}

    // Parameterized constructor
    public UniversityNameId(Long universityId, Long languageId) {
        this.universityId = universityId;
        this.languageId = languageId;
    }

    // Getters and setters
    public Long getUniversityId() {
        return universityId;
    }

    public void setUniversityId(Long universityId) {
        this.universityId = universityId;
    }

    public Long getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Long languageId) {
        this.languageId = languageId;
    }

    
    @Override
    public int hashCode() {
        return Objects.hash(universityId, languageId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UniversityNameId that = (UniversityNameId) o;
        return Objects.equals(universityId, that.universityId) &&
               Objects.equals(languageId, that.languageId);
    }

    @Override
    public String toString() {
        return "UniversityNameId{" +
                "universityId=" + universityId +
                ", languageId=" + languageId +
                '}';
    }
}
