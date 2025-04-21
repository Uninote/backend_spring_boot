package com.uninote.backend.entity;

import javax.persistence.*;

@Entity
@Table(name = "FILE_RESOURCES")
@PrimaryKeyJoinColumn(name = "RESOURCE_PTR_ID")
public class FileResource extends Resource {

    @Column(name = "FILE_URL")
    private String supabaseFileUrl;



    public String getSupabaseFileUrl() {
        return supabaseFileUrl;
    }

    public void setSupabaseFileUrl(String supabaseFileUrl) {
        this.supabaseFileUrl = supabaseFileUrl;
    }

}
