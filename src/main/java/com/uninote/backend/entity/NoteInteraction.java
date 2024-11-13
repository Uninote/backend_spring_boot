package com.uninote.backend.entity;

import javax.persistence.Entity;

public class NoteInteraction {
    private Long userId;
    private Long noteId;
    private int likes;
    private int saves;
    private int views;

    
    public NoteInteraction(Long userId, Long noteId, int likes, int saves, int views) {
        this.userId = userId;
        this.noteId = noteId;
        this.likes = likes;
        this.saves = saves;
        this.views = views;
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

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getSaves() {
        return saves;
    }

    public void setSaves(int saves) {
        this.saves = saves;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }
}
