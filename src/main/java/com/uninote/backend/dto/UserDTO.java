package com.uninote.backend.dto;



public class UserDTO {
    private Long id;
    private String firebaseUid;
    private String name;
    private Long departmentId;
    private Long universityId;
    private String email;
    private String username;
    private String profileImageUrl;
    private Long uniscore;
    private Long roleId;
    private String bio;
    private String rank;
    private int streak;
    private String instagramUsername;
    public UserDTO() {}


    public UserDTO( Long id,String firebaseUid, String name, Long departmentId, Long universityId, String email, String username, String profileImageUrl) {
        this.id = id;
        this.firebaseUid = firebaseUid;
        this.name = name;
        this.departmentId = departmentId;
        this.universityId = universityId;
        this.email = email;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
    }

    public UserDTO( Long id,String firebaseUid,Long uniscore, String name, Long departmentId, Long universityId, String email, String username, String profileImageUrl, Long roleId, String bio) {
        this.id = id;
        this.firebaseUid = firebaseUid;
        this.name = name;
        this.departmentId = departmentId;
        this.universityId = universityId;
        this.email = email;
        this.username = username;
        this.uniscore=uniscore;
        this.profileImageUrl = profileImageUrl;
        this.roleId = roleId;
        this.bio =bio;
    }

    public UserDTO( Long id,String firebaseUid,Long uniscore, String name, Long departmentId, Long universityId, String email, String username, String profileImageUrl, Long roleId, String bio, int streak) {
        this.id = id;
        this.firebaseUid = firebaseUid;
        this.name = name;
        this.departmentId = departmentId;
        this.universityId = universityId;
        this.email = email;
        this.username = username;
        this.uniscore=uniscore;
        this.profileImageUrl = profileImageUrl;
        this.roleId = roleId;
        this.bio =bio;
        this.streak = streak;
    }

    public Long getId(){
        return id;
    }

    public void setId(Long id){
        this.id =id;
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

    public Long getUniscore(){
        return uniscore;
    }
    public  void setUniscore(Long uniscore){
        this.uniscore = uniscore;
    }


    public void setRoleId(Long roleId){
        this.roleId = roleId;
    }

    public Long getRoleId(){
        return roleId;
    }

    public String getBio(){
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }
    public String getRank() {
        return rank;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public String getInstagramUsername() {
        return instagramUsername;
    }

    public void setInstagramUsername(String instagramUsername) {
        this.instagramUsername = instagramUsername;
    }
}
