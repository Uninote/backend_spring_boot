package com.uninote.backend.dto;

public class NoteDTO {
    private Long noteId;
    private Long courseId;
    private Long userId;
    private String title;
    private String description;
    private String pdfUrl;
    private String filename;
    private Boolean isPublic;
            

    
    public NoteDTO(Long noteId,Long courseId, Long userId, String title, String description, String pdfUrl, String filename, Boolean isPublic) {
        this.courseId = courseId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.pdfUrl = pdfUrl;
        this.filename = filename;
        this.isPublic = isPublic;
        this.noteId = noteId;
    }
    
    public NoteDTO() {};

    
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

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public void setNotesId(Long noteId){
        this.noteId = noteId;
    }

    public Long getNoteId(){
        return noteId;
    }
}
