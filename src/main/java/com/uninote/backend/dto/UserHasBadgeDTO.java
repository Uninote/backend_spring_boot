package com.uninote.backend.dto;

public class UserHasBadgeDTO {

    private Long id;
    private Long userId;
    private String name;
    private String description;
    private String imageUrl;
    private int requirement;
    private String typeName;
    private Boolean userHasBadge;

    
    public UserHasBadgeDTO() {}    

    
    public UserHasBadgeDTO(Long id, String name, String description, String imageUrl, Boolean hasBadge, Long uid) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.userHasBadge = hasBadge;
        this.userId = uid;
    }

    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long id) {
        this.userId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getRequirement() {
        return requirement;
    }

    public void setRequirement(int requirement) {
        this.requirement = requirement;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Boolean getUserHasBadge() {
        return userHasBadge;
    }

    public void setUserHasBadge(Boolean userHasBadge) {
        this.userHasBadge = userHasBadge;
    }
}