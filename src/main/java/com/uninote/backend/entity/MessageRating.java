package com.uninote.backend.entity;

public enum MessageRating {
    THUMBS_UP("thumbs_up"),
    THUMBS_DOWN("thumbs_down"),
    FIVE_STARS("5_stars"),
    FOUR_STARS("4_stars"),
    THREE_STARS("3_stars"),
    TWO_STARS("2_stars"),
    ONE_STAR("1_star"),
    HELPFUL("helpful"),
    NOT_HELPFUL("not_helpful"),
    EXCELLENT("excellent"),
    GOOD("good"),
    FAIR("fair"),
    POOR("poor");

    private final String value;

    MessageRating(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static boolean isValidRating(String rating) {
        if (rating == null || rating.trim().isEmpty()) {
            return false;
        }
        
        for (MessageRating validRating : values()) {
            if (validRating.value.equals(rating.trim())) {
                return true;
            }
        }
        return false;
    }

    public static MessageRating fromString(String rating) {
        if (rating == null) {
            return null;
        }
        
        for (MessageRating validRating : values()) {
            if (validRating.value.equals(rating.trim())) {
                return validRating;
            }
        }
        return null;
    }
} 