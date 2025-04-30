package com.uninote.backend.config;

import org.springframework.stereotype.Component;

@Component
public class ContentAccessPolicy {
    public static final int MAX_FREE_NOTE_VIEWS = AppConstants.MAX_FREE_NOTE_VIEWS;

    private ContentAccessPolicy() {
    }

    public boolean isAccessAllowedForAnonymous(int viewedCount) {
        return viewedCount < MAX_FREE_NOTE_VIEWS;
    }

    public boolean isAccessAllowedForUser(int viewedCount, Long uploadedNotes) {
        return uploadedNotes > 0  || viewedCount < MAX_FREE_NOTE_VIEWS;
    }

}
