package com.uninote.backend.interfaceProjection;

import java.time.LocalDateTime;

import com.uninote.backend.dto.NoteDTO;

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
    NoteDTO getFirstNote();
    String getProfessor();      
    String getAcademicYear();  
    Long getNoteTypeId();       
    String getNoteType();
}
