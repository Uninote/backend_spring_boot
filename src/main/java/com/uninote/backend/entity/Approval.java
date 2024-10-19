package com.uninote.backend.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "user_approvals", schema = "ADMIN")
public class Approval implements Serializable {

    @EmbeddedId
    private ApprovalId id;

    @Column(name = "approved_user_id", nullable = false, insertable = false, updatable = false)
    private Long approvedId;

    @Column(name = "user_id", nullable = false, insertable = false, updatable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User approvedUser;

    public Approval() {}

    public Approval(ApprovalId id, Long approvedId, Long userId) {
        this.id = id;
        this.approvedId = approvedId;
        this.userId = userId;
    }

    
    public ApprovalId getId() {
        return id;
    }

    public void setId(ApprovalId id) {
        this.id = id;
    }

    public Long getApprovedId() {
        return approvedId;
    }

    public void setApprovedId(Long approvedId) {
        this.approvedId = approvedId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getApprovedUser() {
        return approvedUser;
    }

    public void setApprovedUser(User approvedUser) {
        this.approvedUser = approvedUser;
    }
}
