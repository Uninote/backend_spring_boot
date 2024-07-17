package com.uninote.backend.entity;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

@Entity
@Table(name = "universities")
public class University {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "university_seq")
    @SequenceGenerator(name = "university_seq", sequenceName = "seq_university_id", allocationSize = 1)
    @Column(name = "university_id", nullable = false, updatable = false)
    private Long id;


    @OneToMany(mappedBy = "university", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Department> departments = new HashSet<>();

    @Column(name = "location", nullable = false)
    private String location;


    @OneToMany(mappedBy = "university", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UniversityName> universityNames = new HashSet<>();

    
    // Getters and setters
    public Long getId() {
        return id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}