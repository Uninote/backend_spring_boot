package com.uninote.backend.dto;

public class DepartmentNameDTO {
    private Long id;
    private String name;
    private String language;
    private String fullName;

    public DepartmentNameDTO() {
    }

    public DepartmentNameDTO(Long id, String name, String language, String fullName) {
        this.id = id;
        this.name = name;
        this.language = language;
        this.fullName = fullName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public void setFullName(String name) {
        this.fullName = name;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
