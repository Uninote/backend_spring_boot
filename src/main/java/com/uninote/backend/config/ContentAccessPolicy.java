package com.uninote.backend.config;

import org.springframework.stereotype.Component;

@Component
public class ContentAccessPolicy {
    public static final int MAX_FREE_NOTE_VIEWS = 5;

    private ContentAccessPolicy() {
    }

    public boolean isAccessAllowed(long viewedCount, boolean hasUploadedNotes) {
        return hasUploadedNotes || viewedCount < MAX_FREE_NOTE_VIEWS;
    }

}
