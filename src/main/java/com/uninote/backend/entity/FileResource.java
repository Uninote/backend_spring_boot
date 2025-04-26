package com.uninote.backend.entity;

import javax.persistence.*;

@Entity
@Table(name = "FILE_RESOURCES")
@PrimaryKeyJoinColumn(name = "RESOURCE_PTR_ID")
public class FileResource extends Resource {

    @Column(name = "FILE_URL")
    private String fileUrl;



    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

}
