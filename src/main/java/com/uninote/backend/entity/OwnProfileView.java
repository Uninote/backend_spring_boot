package com.uninote.backend.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name  = "Own_Profile_Views" )
public class OwnProfileView {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    @Column(name  = "view_id")
    private Long viewId;

    @Column(name = "User_id")
    private Long userId;

    @Column(name = "view_timestamp")
    private LocalDateTime viewTimestamp;

    @Column(name = "session_id")
    private Long sessionId;

    public OwnProfileView() {
        
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setViewTimestamp(LocalDateTime timestamp) {
        this.viewTimestamp = timestamp;
    }

    public LocalDateTime getViewTimestamp() {
        return viewTimestamp;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getSessionId() {
        return sessionId;
    }


}
