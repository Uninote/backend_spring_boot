package com.uninote.backend.dto;

public class NoteCollectionDTO {
    private Long collectionId;
    private Long adminId;
    private String title;
    private String description;
    private Boolean isPublic;

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
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


    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public void setCollectionId(Long collectionId){
        this.collectionId = collectionId;
    }

    public Long getCollectionId(){
        return collectionId;
    }

}
