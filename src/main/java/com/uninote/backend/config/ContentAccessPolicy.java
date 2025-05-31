package com.uninote.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.uninote.backend.entity.User;
import com.uninote.backend.service.NoteService;
import com.uninote.backend.service.NoteViewService;

@Component
public class ContentAccessPolicy {
    public static final int MAX_FREE_NOTE_VIEWS = AppConstants.MAX_FREE_NOTE_VIEWS;

    private static final Logger logger = LoggerFactory.getLogger(ContentAccessPolicy.class);

    @Autowired
    private NoteService noteService;

    @Autowired
    private NoteViewService noteViewService;

    private ContentAccessPolicy() {
    }

    public boolean isAccessAllowedForAnonymous(int viewedCount) {
        boolean allowed = viewedCount < MAX_FREE_NOTE_VIEWS;
        logger.error("Anonymous access check: viewedCount={}, allowed={}", viewedCount, allowed);
        return true;
    }

    public boolean isAccessAllowedForUser(int viewedCount, Long uploadedNotes) {
        boolean allowed = uploadedNotes > 0 || viewedCount < MAX_FREE_NOTE_VIEWS;
        logger.error("User access check: uploadedNotes={}, viewedCount={}, allowed={}", uploadedNotes, viewedCount,
                allowed);
        return true;
    }

    public boolean isAccessAllowedForUser(User user) {

        Long uploadedNotes = noteService.countNotesByUserId(user.getId());
        logger.error(user.getId().toString());
        int viewedCount = noteViewService.getTodayViewCount(user.getId());
        boolean allowed = uploadedNotes > 0 || viewedCount < MAX_FREE_NOTE_VIEWS;
        logger.error("User access check: uploadedNotes={}, viewedCount={}, allowed={}",
                uploadedNotes, viewedCount, allowed);
        return true;

    }
}
