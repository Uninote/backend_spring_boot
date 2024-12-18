package com.uninote.backend.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "NOTE_TYPE_NAMES")
public class NoteTypeName implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    
    @Column(name = "TYPE_ID")
    private Long typeId;

    @Column(name = "TYPE_NAME", nullable = false, length = 50)
    private String typeName;

    @ManyToOne
    @JoinColumn(name = "LANGUAGE_ID", referencedColumnName = "language_id", nullable = false)
    private Language language;
    
    @ManyToOne
    @JoinColumn(name = "TYPE_ID", referencedColumnName = "type_id", insertable = false, updatable = false)
    private NoteType noteType;
    // Getters and Setters
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

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public NoteType getNoteType() {
        return noteType;
    }

    public void setNoteType(NoteType noteType) {
        this.noteType = noteType;
    }
}
