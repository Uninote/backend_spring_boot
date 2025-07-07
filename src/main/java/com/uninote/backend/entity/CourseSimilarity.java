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
@Table(name = "course_similarities" )
public class CourseSimilarity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id_a", nullable = false)
    private Course courseA;

    @ManyToOne
    @JoinColumn(name = "course_id_b", nullable = false)
    private Course courseB;

    @Column(name = "similarity_score")
    private double similarityScore;


    public CourseSimilarity() {}

    public CourseSimilarity(Course courseA, Course courseB, double similarityScore) {
        this.courseA = courseA;
        this.courseB = courseB;
        this.similarityScore = similarityScore;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Course getCourseA() {
        return courseA;
    }

    public void setCourseA(Course courseA) {
        this.courseA = courseA;
    }

    public Course getCourseB() {
        return courseB;
    }

    public void setCourseB(Course courseB) {
        this.courseB = courseB;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
    }
}
