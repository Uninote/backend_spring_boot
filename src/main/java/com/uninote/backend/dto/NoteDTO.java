package com.uninote.backend.dto;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

import com.uninote.backend.validation.NoteValidation.CreateGroup;
import com.uninote.backend.validation.NoteValidation.UpdateGroup;

public class NoteDTO {

    @NotNull(message = "Note ID is required for updates", groups = UpdateGroup.class)
    private Long noteId;
    


    @NotNull(message = "Course ID is required for creation", groups = CreateGroup.class)
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
    private String username;
    private String profileImageUrl;
    private LocalDateTime createdAt;
    private String professor;
    private String academicYear;
    private Long noteTypeId;
    private String noteType;
    private int semester;
    private Boolean certified;

    public NoteDTO(Long noteId, Long courseId, Long userId, String title, String description, String pdfUrl, String filename, Boolean isPublic, 
                   String courseName, String universityName, String departmentName, Long totalLikes, LocalDateTime createdAt) {
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
        this.createdAt = createdAt;
    }

    public NoteDTO(Long noteId, Long courseId, Long userId, String title, String description, String pdfUrl, String filename, Boolean isPublic, 
                   String courseName, String universityName, String departmentName, Long totalLikes, String username, String profileImageUrl, LocalDateTime createdAt) {
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
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.createdAt = createdAt;
                    
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
    

    public NoteDTO(Long noteId, Long courseId, Long userId, String title, String description, String pdfUrl, String filename, Boolean isPublic, 
                   String courseName, String universityName, String departmentName, Long totalLikes, String username, String profileImageUrl, LocalDateTime createdAt, String noteType, String professor, String academicYear) {
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
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.createdAt = createdAt;
        this.noteType = noteType;
        this.professor = professor;
        this.academicYear = academicYear;
                    
    }


    public NoteDTO(Long noteId, Long courseId, Long userId, String title, String description, String pdfUrl, String filename, Boolean isPublic, 
                   String courseName, String universityName, String departmentName, Long totalLikes, String username, String profileImageUrl, LocalDateTime createdAt, String noteType, String professor, String academicYear, Boolean certified) {
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
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.createdAt = createdAt;
        this.noteType = noteType;
        this.professor = professor;
        this.academicYear = academicYear;
        this.certified = certified;
                    
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

    public void setNoteId(Long noteId){
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

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setProfileImageUrl(String profileUrl) {
        this.profileImageUrl = profileUrl;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    public String getProfessor() {
        return professor;
    }
    
    public void setProfessor(String professor) {
        this.professor = professor;
    }
    
    
    public String getAcademicYear() {
        return academicYear;
    }
    
    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }
    
    public Long getNoteTypeId() {
        return noteTypeId;
    }
    
    public void setNoteTypeId(Long noteTypeId) {
        this.noteTypeId = noteTypeId;
    }
    
    public String getNoteType() {
        return noteType;
    }
    
    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public int getSemester() {
        return semester;
    }

    public Boolean getCertified() {
        return certified;
    }

    public void setCertified(Boolean certified){
        this.certified = certified;
    }
}
