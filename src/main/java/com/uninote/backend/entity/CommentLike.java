package com.uninote.backend.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "COMMENT_LIKES", schema = "ADMIN")
public class CommentLike implements Serializable {

    @EmbeddedId
    private CommentLikeId id;

    @ManyToOne
    @MapsId("commentId")
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "liked_at", nullable = false)

    private LocalDateTime likedAt = LocalDateTime.now();
    public CommentLike() {}

    public CommentLike(CommentLikeId id, Comment comment, User user) {
        this.id = id;
        this.comment = comment;
        this.user = user;
        this.likedAt = LocalDateTime.now();
    }
    
    public CommentLikeId getId() {
        return id;
    }

    public void setId(CommentLikeId id) {
        this.id = id;
    }

    public LocalDateTime getLikedAt() {
        return likedAt;
    }

    public void setLikedAt(LocalDateTime likedAt) {
        this.likedAt = likedAt;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}


