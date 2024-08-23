package com.uninote.backend.interfaceProjection;

import java.time.LocalDateTime;

public interface NoteProjection {
    Long getId();
    Long getCourseId();
    Long getUserId();
    String getTitle();
    String getDescription();
    String getPdfUrl();
    String getFilename();
    String getCourseName();
    String getUniversityName();
    String getDepartmentName();
    Long getLikes();
    String getUsername();
    String getProfileImageUrl();
    LocalDateTime getCreatedAt();
}
