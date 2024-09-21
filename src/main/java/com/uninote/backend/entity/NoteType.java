package com.uninote.backend.entity;

import javax.persistence.*;

@Entity
@Table(name = "note_types", schema = "admin")
public class NoteType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "type_id", nullable = false, updatable = false)
    private Long typeId;

    @Column(name = "type_name", nullable = false, length = 50)
    private String typeName;


    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
