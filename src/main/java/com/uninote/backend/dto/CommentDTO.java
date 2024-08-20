package com.uninote.backend.dto;

import java.time.LocalDateTime;

public class CommentDTO {

    private Long commentId;
    private Long noteId;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
    private Long totalLikes;
    public CommentDTO() {
    }

    public CommentDTO(Long commentId, Long noteId, Long userId, String content, LocalDateTime createdAt) {
        this.commentId = commentId;
        this.noteId = noteId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public Long getNoteId() {
        return noteId;
    }

    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getTotalLikes() {
        return totalLikes;
    }

    public void setTotalLikes(Long totalLikes) {
        this.totalLikes = totalLikes;
    }
}
