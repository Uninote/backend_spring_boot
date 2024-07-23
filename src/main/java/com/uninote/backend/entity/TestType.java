package com.uninote.backend.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "test_types")
public class TestType {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "test_types_seq")
    @SequenceGenerator(name = "test_types_seq", sequenceName = "test_types_seq", allocationSize = 1)
    private Long id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "testType")
    private List<Test> tests;

    
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

    public List<Test> getTests() {
        return tests;
    }

    public void setTests(List<Test> tests) {
        this.tests = tests;
    }
}
