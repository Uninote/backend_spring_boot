package com.uninote.backend.message;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageType;

public class UserImageMessage implements ChatMessage {

    private final String imageUrl;

    public UserImageMessage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String imageUrl() {
        return imageUrl;
    }

    public String role() {
        return "user"; 
    }

    @Override
    public ChatMessageType type() {
        return ChatMessageType.USER;
    }

    @Override
    public String text() {
        return imageUrl != null ? imageUrl : "[Image]";
    }

    @Override
    public String toString() {
        return "UserImageMessage{" +
               "imageUrl='" + imageUrl + '\'' +
               '}';
    }
}
