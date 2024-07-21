package com.uninote.backend.dto;

import java.time.LocalDateTime;

import com.uninote.backend.entity.Note;

public class NoteDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String pdfUrl;
    private Long departmentId;
    private Long universityId;
    private int views;
    private int likes;
    private boolean isPublic;

    
    
    public NoteDTO(Long id, String title, String description, LocalDateTime createdAt, LocalDateTime updatedAt, String pdfUrl, Long departmentId, Long universityId, int views, int likes, boolean isPublic) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.pdfUrl = pdfUrl;
        this.departmentId = departmentId;
        this.universityId = universityId;
        this.views = views;
        this.likes = likes;
        this.isPublic = isPublic;
    }

   
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Long getUniversityId() {
        return universityId;
    }

    public void setUniversityId(Long universityId) {
        this.universityId = universityId;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }
    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }


    public NoteDTO convertToDTO(Note note) {
        return new NoteDTO(
                note.getId(),
                note.getTitle(),
                note.getDescription(),
                note.getCreatedAt(),
                note.getUpdatedAt(),
                note.getPdfUrl(),
                note.getCourse().getDepartment().getId(),
                note.getCourse().getDepartment().getUniversity().getId(),
                note.getViews(),
                note.getLikes(),
                note.getIsPublic()
        );
    }
}
