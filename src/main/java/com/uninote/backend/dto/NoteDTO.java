package com.uninote.backend.dto;

public class NoteDTO {
    private Long courseId;
    private Long userId;
    private String title;
    private String description;
    private String pdfUrl;
    private String filename;
    private int views;
    private int likes;
    private boolean isPublic;

    
    public NoteDTO(Long courseId, Long userId, String title, String description, String pdfUrl, String filename, int views, int likes, boolean isPublic) {
        this.courseId = courseId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.pdfUrl = pdfUrl;
        this.filename = filename;
        this.views = views;
        this.likes = likes;
        this.isPublic = isPublic;
    }

    
    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFilename(){
        return this.filename;
    }

    public void setFilename(String filename){
        this.filename = filename;
    }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
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

    public boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
}
