package com.uninote.backend.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

@Entity
@IdClass(UserNoteHelpedId.class)
@Table(name = "user_note_has_helped", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "note_id"})
})
public class UserNoteHelped {

    @Id
    @Column(name = "note_id")
    private Long noteId;

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "helped", nullable = false)
    private Boolean helped = false;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "interaction_date", nullable = false, updatable = false)
    private Date interactionDate = new Date();

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "note_id", insertable = false, updatable = false)
    private Note note;


    public UserNoteHelped() {}

    public UserNoteHelped(Long noteId, Long userId, Boolean helped) {
        this.noteId = noteId;
        this.userId = userId;
        this.helped = helped;
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

    public Boolean getHelped() {
        return helped;
    }

    public void setHelped(Boolean helped) {
        this.helped = helped;
    }

    public Date getInteractionDate() {
        return interactionDate;
    }

    public void setInteractionDate(Date interactionDate) {
        this.interactionDate = interactionDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return "UserNoteHelped{" +
                "noteId=" + noteId +
                ", userId=" + userId +
                ", helped=" + helped +
                ", interactionDate=" + interactionDate +
                '}';
    }
}
