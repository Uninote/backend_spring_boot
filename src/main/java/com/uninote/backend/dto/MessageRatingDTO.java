package com.uninote.backend.dto;

public class MessageRatingDTO {
    private Long messageId;
    private String rating;

    public MessageRatingDTO() {}

    public MessageRatingDTO(Long messageId, String rating) {
        this.messageId = messageId;
        this.rating = rating;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }
} 