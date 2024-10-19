package com.uninote.backend.entity;


import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "profile_views", schema = "ADMIN")
public class ProfileView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "VIEW_ID")
    private Long viewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VIEWER_ID", nullable = false)
    private User viewer; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROFILE_ID", nullable = false)
    private User profileOwner;  

    @Column(name = "VIEW_DATE", nullable = false)
    private LocalDateTime viewDate = LocalDateTime.now();  

    @Column(name = "SESSION_ID", nullable = true)
    private Long sessionId;  

   
    public Long getViewId() {
        return viewId;
    }

    public void setViewId(Long viewId) {
        this.viewId = viewId;
    }

    public User getViewer() {
        return viewer;
    }

    public void setViewer(User viewer) {
        this.viewer = viewer;
    }

    public User getProfileOwner() {
        return profileOwner;
    }

    public void setProfileOwner(User profileOwner) {
        this.profileOwner = profileOwner;
    }

    public LocalDateTime getViewDate() {
        return viewDate;
    }

    public void setViewDate(LocalDateTime viewDate) {
        this.viewDate = viewDate;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }
}
