package com.uninote.backend.dto;

import com.uninote.backend.entity.Resource;
import com.uninote.backend.entity.FileResource;
import com.uninote.backend.entity.YouTubeResource;
import com.uninote.backend.entity.NoteResource;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FileSelectionDTO {
    private Long id;
    private String title;
    private String summary;
    private String type;
    private Double relevanceScore;
    private Timestamp createdAt;
    private String fileUrl;
    private String youtubeUrl;
    private String notePdfUrl;
    private String content;

    public FileSelectionDTO(Resource resource, Double relevanceScore) {
        this.id = resource.getId();
        this.title = resource.getTitle();
        this.summary = resource.getSummary();
        this.relevanceScore = relevanceScore;
        this.createdAt = resource.getCreatedAt();
        this.type = getResourceType(resource);
        this.content = resource.getContent();

        // Set type-specific URLs
        if (resource instanceof FileResource) {
            this.fileUrl = ((FileResource) resource).getFileUrl();
        } else if (resource instanceof YouTubeResource) {
            this.youtubeUrl = ((YouTubeResource) resource).getYoutubeUrl();
        } else if (resource instanceof NoteResource) {
            this.notePdfUrl = ((NoteResource) resource).getNote().getPdfUrl();
        }
    }

    private String getResourceType(Resource resource) {
        if (resource instanceof FileResource) {
            return "PDF Document";
        } else if (resource instanceof YouTubeResource) {
            return "YouTube Video";
        } else if (resource instanceof NoteResource) {
            return "Note";
        } else {
            return "Document";
        }
    }

    // Static method to convert list of resources
    public static List<FileSelectionDTO> fromResources(List<Resource> resources, Map<Long, Double> scores) {
        return resources.stream()
            .map(resource -> new FileSelectionDTO(resource, scores.getOrDefault(resource.getId(), 0.0)))
            .collect(Collectors.toList());
    }

    // Static method to convert list of resources with default scores
    public static List<FileSelectionDTO> fromResources(List<Resource> resources) {
        return resources.stream()
            .map(resource -> new FileSelectionDTO(resource, 0.0))
            .collect(Collectors.toList());
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getRelevanceScore() {
        return relevanceScore;
    }

    public void setRelevanceScore(Double relevanceScore) {
        this.relevanceScore = relevanceScore;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    public String getNotePdfUrl() {
        return notePdfUrl;
    }

    public void setNotePdfUrl(String notePdfUrl) {
        this.notePdfUrl = notePdfUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
} 