package com.uninote.backend.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NoteSearchResponse {
    


    @JsonProperty("content")
    private List<NoteDTO> content;  
    private long totalElements;
    private int totalPages;

    public NoteSearchResponse(List<NoteDTO> content, long totalElements, int totalPages) {
        this.content = content;  
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public List<NoteDTO> getContent() {  
        return content;
    }

    public void setContent(List<NoteDTO> content) {  
        this.content = content;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
