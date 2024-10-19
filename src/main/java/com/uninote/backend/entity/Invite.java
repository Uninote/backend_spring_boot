package com.uninote.backend.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invites", schema = "ADMIN")
public class Invite {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "invite_seq")
    @SequenceGenerator(name = "invite_seq", sequenceName = "invite_seq", allocationSize = 1)
    @Column(name = "invite_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "invitee_id")
    private User invitee;

    @Column(name = "date_of_invite", nullable = false)
    private LocalDateTime dateOfInvite;

    @Column(name = "date_of_sign_up")
    private LocalDateTime dateOfSignUp;

    @Column(name = "uuid_col", nullable = false, unique = true, updatable = false)
    private String uuid;

    public Invite() {
        this.dateOfInvite = LocalDateTime.now();
    }

    public Invite(User user, User invitee, LocalDateTime dateOfSignUp) {
        this.user = user;
        this.invitee = invitee;
        this.dateOfInvite = LocalDateTime.now();
        this.dateOfSignUp = dateOfSignUp;
        this.uuid = UUID.randomUUID().toString(); 
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getInvitee() {
        return invitee;
    }

    public void setInvitee(User invitee) {
        this.invitee = invitee;
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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
