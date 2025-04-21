package com.uninote.backend.dto;

public class FileResourceDTO extends ResourceDTO {
    private String supabaseFileUrl;
    private String mimeType;
    private Long fileSize;

    public FileResourceDTO() {
        this.type = "file";
    }

    // Getters
    public String getSupabaseFileUrl() {
        return supabaseFileUrl;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setSupabaseFileUrl(String supabaseFileUrl) {
        this.supabaseFileUrl = supabaseFileUrl;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
}
