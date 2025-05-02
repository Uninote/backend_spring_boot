package com.uninote.backend.entity;

import javax.persistence.*;

@Entity
@Table(name = "NOTE_RESOURCES")
@PrimaryKeyJoinColumn(name = "RESOURCE_PTR_ID")
public class NoteResource extends Resource {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NOTE_ID", nullable = false)
    private Note note;

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }
}
