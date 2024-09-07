package com.uninote.backend.entity;


import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "save_history")
public class SaveHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "save_history_seq_gen")
    @SequenceGenerator(name = "save_history_seq_gen", sequenceName = "save_history_seq", allocationSize = 1)
    @Column(name = "history_id")
    private Long historyId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "note_id", nullable = false)
    private Long noteId;

    @Column(name = "action", nullable = false)
    private int action;  // 0 for unsave, 1 for save

    @Column(name = "action_time", nullable = false)
    private LocalDateTime actionTime;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private UserSession session;

    // Getters and Setters
    public Long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
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

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public LocalDateTime getActionTime() {
        return actionTime;
    }

    public void setActionTime(LocalDateTime actionTime) {
        this.actionTime = actionTime;
    }

    public UserSession getSession() {
        return session;
    }

    public void setSession(UserSession session) {
        this.session = session;
    }
}
