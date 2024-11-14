package com.uninote.backend.entity;

import java.io.Serializable;
import java.util.Objects;

public class UserNoteMatrixEntryId implements Serializable {

    private Long userId;
    private Long noteId;

    public UserNoteMatrixEntryId() {
    }

    public UserNoteMatrixEntryId(Long userId, Long noteId) {
        this.userId = userId;
        this.noteId = noteId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getNoteId() {
        return noteId;
    }

    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserNoteMatrixEntryId that = (UserNoteMatrixEntryId) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(noteId, that.noteId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, noteId);
    }
}
