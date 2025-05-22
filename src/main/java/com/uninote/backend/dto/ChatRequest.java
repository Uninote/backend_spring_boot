package com.uninote.backend.dto;

import com.uninote.backend.dto.ChatRequest;

public class ChatRequest {
        private String title;

        public ChatRequest(String title) {
            this.title = title;
        }
         public ChatRequest() {
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
        
    }