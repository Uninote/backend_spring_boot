package com.uninote.backend.dto;

import com.uninote.backend.interfaceProjection.NoteProjection;
import java.util.Objects;

public class CollectionDTO {
    private Long collectionId;
    private String name;
    private Boolean isPublic;
    private String adminUsername;
    private Long totalLikes;
    private Long noteNum;
    private NoteProjection firstNote;

    // Constructor
    public CollectionDTO(Long collectionId, String name, Boolean isPublic, String adminUsername, Long totalLikes, Long noteNum, NoteProjection firstNote) {
        this.collectionId = collectionId;
        this.name = name;
        this.isPublic = isPublic;
        this.adminUsername = adminUsername;
        this.totalLikes = totalLikes;
        this.noteNum = noteNum;
        this.firstNote = firstNote;
    }

    // Getters and Setters
    public Long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Long collectionId) {
        this.collectionId = collectionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public Long getTotalLikes() {
        return totalLikes;
    }

    public void setTotalLikes(Long totalLikes) {
        this.totalLikes = totalLikes;
    }

    public Long getNoteNum() {
        return noteNum;
    }

    public void setNoteNum(Long noteNum) {
        this.noteNum = noteNum;
    }

    public NoteProjection getFirstNote() {
        return firstNote;
    }

    public void setFirstNote(NoteProjection firstNote) {
        this.firstNote = firstNote;
    }

    // toString method
    @Override
    public String toString() {
        return "CollectionDTO{" +
                "collectionId=" + collectionId +
                ", name='" + name + '\'' +
                ", isPublic=" + isPublic +
                ", adminUsername='" + adminUsername + '\'' +
                ", totalLikes=" + totalLikes +
                ", noteNum=" + noteNum +
                ", firstNote=" + firstNote +
                '}';
    }

    // equals and hashCode methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CollectionDTO that = (CollectionDTO) o;
        return Objects.equals(collectionId, that.collectionId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(isPublic, that.isPublic) &&
                Objects.equals(adminUsername, that.adminUsername) &&
                Objects.equals(totalLikes, that.totalLikes) &&
                Objects.equals(noteNum, that.noteNum) &&
                Objects.equals(firstNote, that.firstNote);
    }

    @Override
    public int hashCode() {
        return Objects.hash(collectionId, name, isPublic, adminUsername, totalLikes, noteNum, firstNote);
    }
}
