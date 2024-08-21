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
    private String courseName;
    private String universityName;
    private String departmentName;
    private Long totalLikes;

    public NoteDTO(Long noteId, Long courseId, Long userId, String title, String description, String pdfUrl, String filename, Boolean isPublic, 
                   String courseName, String universityName, String departmentName, Long totalLikes) {
        this.noteId = noteId;
        this.courseId = courseId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.pdfUrl = pdfUrl;
        this.filename = filename;
        this.isPublic = isPublic;
        this.courseName = courseName;
        this.universityName = universityName;
        this.departmentName = departmentName;
        this.totalLikes = totalLikes;
    }

    
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

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getUniversityName() {
        return universityName;
    }

    public void setUniversityName(String universityName) {
        this.universityName = universityName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public void setTotalLikes(Long totalLikes) {
        this.totalLikes = totalLikes;
    }

    public Long getTotalLikes() {
        return totalLikes;
    }
}
