package com.uninote.backend.entity;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@IdClass(UserNoteMatrixEntryId.class)
@Table(name = "USER_NOTE_INTERACTION_MATRIX")
public class UserNoteMatrixEntry implements Serializable{
    @Id
    @Column(name = "user_id")
    private Long userId;
    @Id
    @Column(name= "note_id")
    private Long noteId;

    @Column(name = "interaction_score")
    private double interactionScore;

    public UserNoteMatrixEntry() {}

    public UserNoteMatrixEntry(Long userId, Long noteId, double interactionScore) {
        this.userId = userId;
        this.noteId = noteId;
        this.interactionScore = interactionScore;
    }

    public Long getNoteId() {
        return noteId;
    }


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setNoteId(Long noteId) {
        this.noteId= noteId;
    }


    public void setInteractionScore(double interactionScore) {
        this.interactionScore= interactionScore;
    } 

    public double getInteractionScore() {
        return interactionScore;
    }

}
