package com.uninote.backend.dto;

import java.util.Map;

public class MessageRatingDTO {
    private Long messageId;
    private Map<String, Object> rating;

    public MessageRatingDTO() {}

    public MessageRatingDTO(Long messageId, Map<String, Object> rating) {
        this.messageId = messageId;
        this.rating = rating;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Map<String, Object> getRating() {
        return rating;
    }

    public void setRating(Map<String, Object> rating) {
        this.rating = rating;
    }
} 