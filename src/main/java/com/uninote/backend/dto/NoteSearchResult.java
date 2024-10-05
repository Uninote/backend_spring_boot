package com.uninote.backend.dto;

public class NoteSearchResult {

    private Long id;
    private Long courseId;
    private Long userId;
    private String title;
    private String description;
    private String pdfUrl;
    private String filename;
    private boolean isPublic;
    private String courseName;
    private String universityName;
    private String departmentName;
    private Integer likes;
    private String username;
    private String profileImageUrl;
    private String createdAt;
    private String professor;
    private String academicYear;
    private String typeName;
    private Integer relevanceScore;
    private Long totalElements;
    private Integer totalPages;

    // Default Constructor
    public NoteSearchResult() {
    }

    // Parameterized Constructor
    public NoteSearchResult(Long id, Long courseId, Long userId, String title, String description, String pdfUrl, String filename, boolean isPublic, String courseName, String universityName, String departmentName, Integer likes, String username, String profileImageUrl, String createdAt, String professor, String academicYear, String typeName, Integer relevanceScore, Long totalElements, Integer totalPages) {
        this.id = id;
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
        this.likes = likes;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.createdAt = createdAt;
        this.professor = professor;
        this.academicYear = academicYear;
        this.typeName = typeName;
        this.relevanceScore = relevanceScore;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
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

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
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

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Integer getRelevanceScore() {
        return relevanceScore;
    }

    public void setRelevanceScore(Integer relevanceScore) {
        this.relevanceScore = relevanceScore;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    // toString Method
    @Override
    public String toString() {
        return "NoteSearchResult{" +
                "id=" + id +
                ", courseId=" + courseId +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", pdfUrl='" + pdfUrl + '\'' +
                ", filename='" + filename + '\'' +
                ", isPublic=" + isPublic +
                ", courseName='" + courseName + '\'' +
                ", universityName='" + universityName + '\'' +
                ", departmentName='" + departmentName + '\'' +
                ", likes=" + likes +
                ", username='" + username + '\'' +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", professor='" + professor + '\'' +
                ", academicYear='" + academicYear + '\'' +
                ", typeName='" + typeName + '\'' +
                ", relevanceScore=" + relevanceScore +
                ", totalElements=" + totalElements +
                ", totalPages=" + totalPages +
                '}';
    }
}
