package com.uninote.backend.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_logins", schema = "ADMIN")
public class UserLogin {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_logins_seq")
    @SequenceGenerator(name = "user_logins_seq", sequenceName = "user_logins_seq", allocationSize = 1)
    @Column(name = "user_login_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "login_timestamp", nullable = false)
    private LocalDateTime loginTimestamp;

    
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

    public LocalDateTime getLoginTimestamp() {
        return loginTimestamp;
    }

    public void setLoginTimestamp(LocalDateTime loginTimestamp) {
        this.loginTimestamp = loginTimestamp;
    }
}
