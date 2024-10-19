package com.uninote.backend.entity;

import javax.persistence.*;

@Entity
@Table(name = "uniscore_increase_types", schema = "ADMIN")
public class UniscoreIncreaseType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_name", nullable = false)
    private String typeName;

    @Column(name = "increase_amount", nullable = false)
    private int increaseAmount;

    // Constructors, getters, and setters

    public UniscoreIncreaseType() {}

    public UniscoreIncreaseType(String typeName, int increaseAmount) {
        this.typeName = typeName;
        this.increaseAmount = increaseAmount;
    }

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

    public int getIncreaseAmount() {
        return increaseAmount;
    }

    public void setIncreaseAmount(int increaseAmount) {
        this.increaseAmount = increaseAmount;
    }
}
