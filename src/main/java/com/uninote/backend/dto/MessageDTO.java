package com.uninote.backend.dto;

import java.sql.Timestamp;

import com.uninote.backend.entity.Message;

public class MessageDTO {
    private String userMessage;
    private String serviceResponse;
    private Timestamp createdAt;

    public MessageDTO(Message msg) {
        this.userMessage = msg.getUserMessage();
        this.serviceResponse = msg.getServiceResponse();
        this.createdAt = msg.getCreatedAt();
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

    // Optional: for debugging/logging
    @Override
    public String toString() {
        return "MessageDTO{" +
                "userMessage='" + userMessage + '\'' +
                ", serviceResponse='" + serviceResponse + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}