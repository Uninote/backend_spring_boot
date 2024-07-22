package com.uninote.backend.dto;


public class UserDTO {
    private String firebaseUid;
    private String name;
    private Long departmentId;
    private Long universityId;
    private String email;
    private String username;
    private String profileImageUrl;

    
    public UserDTO() {}

    
    public UserDTO(String firebaseUid, String name, Long departmentId, Long universityId, String email, String username, String profileImageUrl) {
        this.firebaseUid = firebaseUid;
        this.name = name;
        this.departmentId = departmentId;
        this.universityId = universityId;
        this.email = email;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
    }

    

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
}
