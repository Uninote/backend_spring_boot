package com.uninote.backend.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "note_saves")
public class NoteSave {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "save_seq")
    @SequenceGenerator(name = "save_seq", sequenceName = "seq_note_save_id", allocationSize = 1)
    @Column(name = "save_id", nullable = false, updatable = false)
    private Long saveId;

    @Column(name = "note_id", nullable = false)
    private Long noteId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    public NoteSave() {
        this.createdAt = LocalDateTime.now();
    }

    public NoteSave(Long noteId, Long userId) {
        this.noteId = noteId;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }

    
    public Long getSaveId() {
        return saveId;
    }

    public void setSaveId(Long saveId) {
        this.saveId = saveId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
}
