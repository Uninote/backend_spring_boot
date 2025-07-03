package com.uninote.backend.entity;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

@Entity
@Table(name = "department_names" )
public class DepartmentName {

    @EmbeddedId
    private DepartmentNameId id;

    @ManyToOne
    @MapsId("departmentId")
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne
    @MapsId("languageId")
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Column(name = "department_name", nullable = false)
    private String name;

    @Column(name = "department_full_name")
    private String fullName;

    
    public DepartmentName() {}

    public DepartmentName(DepartmentNameId id, Department department, Language language, String name, String fullName) {
        this.id = id;
        this.department = department;
        this.language = language;
        this.name = name;
        this.fullName = fullName;
    }

    
    public DepartmentNameId getId() {
        return id;
    }

    public void setId(DepartmentNameId id) {
        this.id = id;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
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
