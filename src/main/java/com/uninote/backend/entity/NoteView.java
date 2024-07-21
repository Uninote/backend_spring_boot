package com.uninote.backend.entity;

import javax.persistence.*;

@Entity
@Table(name = "note_views")
public class NoteView {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "note_view_seq")
    @SequenceGenerator(name = "note_view_seq", sequenceName = "seq_note_view_id", allocationSize = 1)
    @Column(name = "note_view_id")
    private Long id;

    @Column(name = "note_id", nullable = false)
    private Long noteId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "view_count", nullable = false)
    private Long viewCount;

    // Getters and setters
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

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }
}
