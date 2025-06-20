package com.uninote.backend.dto;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import com.uninote.backend.entity.Message;

public class MessageDTO {
    private Long messageId;
    private String userMessage;
    private String serviceResponse;
    private Timestamp createdAt;
    private String rating;
    private List<MessageMediaDTO> media;

    public MessageDTO(Message msg) {
        this.messageId = msg.getId();
        this.userMessage = msg.getUserMessage();
        this.serviceResponse = msg.getServiceResponse();
        this.createdAt = msg.getCreatedAt();
        this.rating = msg.getRating();
        this.media = msg.getMedia().stream()
            .map(MessageMediaDTO::new)
            .collect(Collectors.toList());
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
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

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
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
                "messageId=" + messageId +
                ", userMessage='" + userMessage + '\'' +
                ", serviceResponse='" + serviceResponse + '\'' +
                ", createdAt=" + createdAt +
                ", rating='" + rating + '\'' +
                ", media=" + media +
                '}';
    }
}