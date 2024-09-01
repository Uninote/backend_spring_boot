package com.uninote.backend.dto;

public class ApprovalDTO {
    
    private Long userId;
    private Long approvedId;
    
    // Constructors
    public ApprovalDTO() {}

    public ApprovalDTO(Long userId, Long approvedId) {
        this.userId = userId;
        this.approvedId = approvedId;
    }

    
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
}
