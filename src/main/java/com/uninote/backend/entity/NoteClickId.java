package com.uninote.backend.entity;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class NoteClickId implements Serializable {

    private Long noteId;
    private Long userId;

    
    
    public NoteClickId() {}

    
    public NoteClickId(Long noteId, Long userId) {
        this.noteId = noteId;
        this.userId = userId;
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

    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoteClickId that = (NoteClickId) o;
        return Objects.equals(noteId, that.noteId) &&
               Objects.equals(userId, that.userId);
    }

    
    @Override
    public int hashCode() {
        return Objects.hash(noteId, userId);
    }

    @Override
    public String toString() {
        return "NoteClickId{" +
                "noteId=" + noteId +
                ", userId=" + userId +
                '}';
    }
}
