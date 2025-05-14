package com.uninote.backend.dto;

import java.util.List;

public class YouTubeResourceDTO extends ResourceDTO {
    private String youtubeUrl;
    private String videoId;
    private String language;
    private List<TranscriptSnippetDto> transcriptSnippets;

    public YouTubeResourceDTO() {
        this.type = "youtube";
    }

    // Getters
    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public String getVideoId() {
        return videoId;
    }

    public String getLanguage() {
        return language;
    }

    public List<TranscriptSnippetDto> getTranscriptSnippets() {
        return transcriptSnippets;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setTranscriptSnippets(List<TranscriptSnippetDto> transcriptSnippets) {
    this.transcriptSnippets = transcriptSnippets;
}
}
