package com.uninote.backend.entity;

import javax.persistence.*;

@Entity
@Table(name = "PUSH_SUBSCRIPTIONS")
public class PushSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Column(name = "ENDPOINT", nullable = false, columnDefinition = "text")
    private String endpoint;

    @Column(name = "P256DH", nullable = false, length = 2048)
    private String p256dh;

    @Column(name = "AUTH", nullable = false, length = 2048)
    private String auth;

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

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getP256dh() {
        return p256dh;
    }

    public void setP256dh(String p256dh) {
        this.p256dh = p256dh;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }
}

