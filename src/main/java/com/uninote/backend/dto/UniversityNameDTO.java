package com.uninote.backend.dto;

public class UniversityNameDTO {
    private Long universityId;
    private Long languageId;
    private String name;
    private String fullName;

    public UniversityNameDTO(Long universityId, Long languageId, String name, String fullName) {
        this.universityId = universityId;
        this.languageId = languageId;
        this.name = name;
        this.fullName = fullName;
    }

    

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
