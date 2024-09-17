package com.uninote.backend.entity;

import javax.persistence.*;

@Entity
@Table(name = "question_types", schema = "ADMIN")
public class QuestionType {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "question_type_seq")
    @SequenceGenerator(name = "question_type_seq", sequenceName = "question_type_seq", allocationSize = 1)
    @Column(name = "question_type_id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "type_name", nullable = false)
    private String typeName;

    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
