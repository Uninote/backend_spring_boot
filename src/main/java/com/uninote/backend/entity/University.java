package com.uninote.backend.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "universities" )
public class University {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "university_seq")
    @SequenceGenerator(name = "university_seq", sequenceName = "university_seq", allocationSize = 1)
    @Column(name = "university_id", nullable = false, updatable = false)
    private Long id;


    @OneToMany(mappedBy = "university", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Department> departments = new HashSet<>();

    @Column(name = "location", nullable = false)
    private String location;


    @OneToMany(mappedBy = "university", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UniversityName> universityNames = new HashSet<>();

    public Set<Department> getDepartments(){
        return departments;
    }

    public void setDepartments(Set<Department> departments){
        this.departments = departments;
    }
    
    public Long getId() {
        return id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
    public Set<UniversityName> getUniversityNames() {
        return universityNames;
    }

    public void setUniversityNames(Set<UniversityName> universityNames) {
        this.universityNames = universityNames;
    }
}