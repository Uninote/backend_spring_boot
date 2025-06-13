package com.uninote.backend.dto;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import com.uninote.backend.entity.Message;
import com.uninote.backend.entity.MessageMedia;

public class MessageDTO {
    private String userMessage;
    private String serviceResponse;
    private Timestamp createdAt;
    private List<MessageMediaDTO> media;

    public MessageDTO(Message msg) {
        this.userMessage = msg.getUserMessage();
        this.serviceResponse = msg.getServiceResponse();
        this.createdAt = msg.getCreatedAt();
        this.media = msg.getMedia().stream()
            .map(MessageMediaDTO::new)
            .collect(Collectors.toList());
    }

    public String getUserMessage() {
        return userMessage;
    }

    public String getServiceResponse() {
        return serviceResponse;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public List<MessageMediaDTO> getMedia() {
        return media;
    }

    // Setters (optional if you're only using it for read/response)
    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public void setServiceResponse(String serviceResponse) {
        this.serviceResponse = serviceResponse;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void setMedia(List<MessageMediaDTO> media) {
        this.media = media;
    }

    // Optional: for debugging/logging
    @Override
    public String toString() {
        return "MessageDTO{" +
                "userMessage='" + userMessage + '\'' +
                ", serviceResponse='" + serviceResponse + '\'' +
                ", createdAt=" + createdAt +
                ", media=" + media +
                '}';
    }
}