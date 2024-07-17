package com.uninote.backend.entity;

import javax.persistence.*;

@Entity
@Table(name = "university_names")
public class UniversityName {

    @EmbeddedId
    private UniversityNameId id;

    @ManyToOne
    @MapsId("universityId")
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

    @ManyToOne
    @MapsId("languageId")
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Column(name = "university_name", nullable = false)
    private String name;

    @Column(name = "university_full_name")
    private String fullName;

    // Constructors
    public UniversityName() {}

    public UniversityName(UniversityNameId id, University university, Language language, String name, String fullName) {
        this.id = id;
        this.university = university;
        this.language = language;
        this.name = name;
        this.fullName = fullName;
    }

    // Getters and setters
    public UniversityNameId getId() {
        return id;
    }

    public void setId(UniversityNameId id) {
        this.id = id;
    }

    public University getUniversity() {
        return university;
    }

    public void setUniversity(University university) {
        this.university = university;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
