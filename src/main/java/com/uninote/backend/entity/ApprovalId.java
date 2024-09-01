package com.uninote.backend.entity;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ApprovalId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "approved_user_id")
    private Long approvedId;

    
    public ApprovalId() {}

    public ApprovalId(Long userId, Long approvedId) {
        this.userId = userId;
        this.approvedId = approvedId;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getApprovedId() {
        return approvedId;
    }

    public void setApprovedId(Long approvedId) {
        this.approvedId = approvedId;
    }

    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApprovalId that = (ApprovalId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(approvedId, that.approvedId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, approvedId);
    }
}
