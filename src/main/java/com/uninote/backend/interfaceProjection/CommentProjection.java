package com.uninote.backend.interfaceProjection;

import java.time.LocalDateTime;

public interface CommentProjection {
    Long getCommentId();
    Long getNoteId();
    Long getUserId();
    String getContent();
    LocalDateTime getCreatedAt();
    Long getTotalLikes();
    String getProfileImageUrl();
    String getUsername();
}