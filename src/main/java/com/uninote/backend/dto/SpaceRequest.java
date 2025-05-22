package com.uninote.backend.dto;

public class SpaceRequest {

    private String title;

    public SpaceRequest() {
    }

    public SpaceRequest(String title) {
        this.title = title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }
    
}
