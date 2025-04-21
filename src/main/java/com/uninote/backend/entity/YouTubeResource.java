package com.uninote.backend.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "YOUTUBE_RESOURCES")
@PrimaryKeyJoinColumn(name = "RESOURCE_ID")
public class YouTubeResource extends Resource {

    @Column(name = "URL")
    private String youtubeUrl;




    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

}
