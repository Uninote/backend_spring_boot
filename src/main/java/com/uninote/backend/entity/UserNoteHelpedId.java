package com.uninote.backend.entity;


import java.io.Serializable;
import java.util.Objects;

public class UserNoteHelpedId implements Serializable {

    private Long userId;
    private Long noteId;

    public UserNoteHelpedId() {}

    public UserNoteHelpedId(Long userId, Long noteId) {
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
        UserNoteHelpedId that = (UserNoteHelpedId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(noteId, that.noteId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, noteId);
    }
}
