package com.uninote.backend.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "SPACE_CHATS")
public class SpaceChat extends Chat{

    @Id
    @Column(name = "CHAT_ID")
    private Long id;


    @ManyToOne
    @JoinColumn(name = "SPACE_ID", nullable = false)
    private Space space;


    public SpaceChat() {}


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Space getSpace() {
        return space;
    }

    public void setSpace(Space space) {
        this.space = space;
    }

    @Override
    public String toString() {
        return "SpaceChat{" +
                "chatId=" + id +
                ", spaceId=" + (space != null ? space.getId() : null) +
                '}';
    }
}
