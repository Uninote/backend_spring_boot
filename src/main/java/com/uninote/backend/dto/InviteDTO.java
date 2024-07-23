package com.uninote.backend.dto;

import java.time.LocalDateTime;

public class InviteDTO {

    private Long id;
    private Long userId;
    private Long inviteeId;
    private LocalDateTime dateOfInvite;
    private LocalDateTime dateOfSignUp;

    public InviteDTO() {
    }

    public InviteDTO(Long id, Long userId, Long inviteeId, LocalDateTime dateOfInvite, LocalDateTime dateOfSignUp) {
        this.id = id;
        this.userId = userId;
        this.inviteeId = inviteeId;
        this.dateOfInvite = dateOfInvite;
        this.dateOfSignUp = dateOfSignUp;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getInviteeId() {
        return inviteeId;
    }

    public void setInviteeId(Long inviteeId) {
        this.inviteeId = inviteeId;
    }

    public LocalDateTime getDateOfInvite() {
        return dateOfInvite;
    }

    public void setDateOfInvite(LocalDateTime dateOfInvite) {
        this.dateOfInvite = dateOfInvite;
    }

    public LocalDateTime getDateOfSignUp() {
        return dateOfSignUp;
    }

    public void setDateOfSignUp(LocalDateTime dateOfSignUp) {
        this.dateOfSignUp = dateOfSignUp;
    }
}
