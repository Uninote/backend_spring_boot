package com.uninote.backend.dto;


public class NoteResourceDTO extends ResourceDTO {
    private String fileUrl;
    private String mimeType;
    private Long fileSize;

    public NoteResourceDTO() {
        this.type = "note";
    }

    // Getters
    public String getFileUrl() {
        return fileUrl;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
}
