package com.uninote.backend.dto;

import java.time.LocalDateTime;

public interface NoteDTO {
    Long getId();
    String getTitle();
    String getDescription();
    LocalDateTime getCreatedAt();
    String getPdfUrl();
}