package com.uninote.backend.dto;

public class UserStatsDTO {
    private long totalNotes;
    private long totalPublicNotes;
    private long totalLikes;

    public UserStatsDTO(long totalNotes, long totalPublicNotes, long totalLikes) {
        this.totalNotes = totalNotes;
        this.totalPublicNotes = totalPublicNotes;
        this.totalLikes = totalLikes;
    }

    // Getters and setters
    public long getTotalNotes() {
        return totalNotes;
    }

    public void setTotalNotes(long totalNotes) {
        this.totalNotes = totalNotes;
    }

    public long getTotalPublicNotes() {
        return totalPublicNotes;
    }

    public void setTotalPublicNotes(long totalPublicNotes) {
        this.totalPublicNotes = totalPublicNotes;
    }

    public long getTotalLikes() {
        return totalLikes;
    }

    public void setTotalLikes(long totalLikes) {
        this.totalLikes = totalLikes;
    }
}
