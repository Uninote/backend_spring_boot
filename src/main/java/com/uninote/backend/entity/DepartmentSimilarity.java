package com.uninote.backend.entity;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "department_similarities" )
public class DepartmentSimilarity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "department_id_a", nullable = false)
    private Department departmentA;

    @ManyToOne
    @JoinColumn(name = "department_id_b", nullable = false)
    private Department departmentB;

    @Column(name = "similarity_score")
    private double similarityScore;

    public DepartmentSimilarity() {}

    public DepartmentSimilarity(Department departmentA, Department departmentB, double similarityScore) {
        this.departmentA = departmentA;
        this.departmentB = departmentB;
        this.similarityScore = similarityScore;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Department getDepartmentA() {
        return departmentA;
    }

    public void setDepartmentA(Department departmentA) {
        this.departmentA = departmentA;
    }

    public Department getDepartmentB() {
        return departmentB;
    }

    public void setDepartmentB(Department departmentB) {
        this.departmentB = departmentB;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
    }
}
