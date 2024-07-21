package com.uninote.backend.entity;

import javax.persistence.*;

@Entity
@Table(name = "note_clicks")
public class NoteClick {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "click_seq")
    @SequenceGenerator(name = "click_seq", sequenceName = "seq_note_click_id", allocationSize = 1)
    @Column(name = "note_click_id")
    private Long id;

    @Column(name = "note_id", nullable = false)
    private Long noteId;

    @Column(name = "user_Id", nullable = false)
    private Long userId;


    @Column(name = "click_count", nullable = false)
    private Long clickCount;

    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNoteId() {
        return noteId;
    }

    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getClickCount() {
        return clickCount;
    }

    public void setClickCount(Long clickCount) {
        this.clickCount = clickCount;
    }
}
