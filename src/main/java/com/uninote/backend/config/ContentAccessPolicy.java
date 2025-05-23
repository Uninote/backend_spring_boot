package com.uninote.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.uninote.backend.entity.User;
import com.uninote.backend.repository.NoteRepository;
import com.uninote.backend.service.NoteService;
import com.uninote.backend.service.NoteViewService;

@Component
public class ContentAccessPolicy {
    public static final int MAX_FREE_NOTE_VIEWS = AppConstants.MAX_FREE_NOTE_VIEWS;

    @Autowired
    private NoteService noteService;

    @Autowired NoteViewService noteViewService;

    private ContentAccessPolicy() {
    }

    public boolean isAccessAllowedForAnonymous(int viewedCount) {
        return viewedCount < MAX_FREE_NOTE_VIEWS;
    }

    public boolean isAccessAllowedForUser(int viewedCount, Long uploadedNotes) {
        return uploadedNotes > 0  || viewedCount < MAX_FREE_NOTE_VIEWS;
    }

    public boolean isAccessAllowedForUser(User user) {
        if (user.getId() == 112L){
            Long uploadedNotes  = noteService.countNotesByUserId(user.getId());
            int viewedCount  = noteViewService.getTodayViewCount(user.getId());
            return uploadedNotes > 0  || viewedCount < MAX_FREE_NOTE_VIEWS;
        }
        return true;

    }

}
