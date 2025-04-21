package com.uninote.backend.entity;

import javax.persistence.*;

@Entity
@Table(name = "RESOURCE_CHATS")
public class ResourceChat {

    @Id
    @Column(name = "CHAT_PTR_ID")
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "CHAT_PTR_ID")
    private Chat chat;

    @ManyToOne
    @JoinColumn(name = "RESOURCE_ID", nullable = false)
    private Resource resource;

    public ResourceChat() {}

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public String toString() {
        return "ResourceChat{" +
                "chatId=" + id +
                ", resourceId=" + (resource != null ? resource.getId() : null) +
                '}';
    }
}
