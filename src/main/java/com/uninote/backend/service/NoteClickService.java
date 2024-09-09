package com.uninote.backend.service;

import com.uninote.backend.entity.NoteClick;
import com.uninote.backend.entity.UserSession;
import com.uninote.backend.repository.NoteClickRepository;
import com.uninote.backend.repository.UserSessionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class NoteClickService {
    @Autowired
    private NoteClickRepository noteClickRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    
    @Async
    public void trackClick(Long noteId, Long userId, Long sessionId) {
        NoteClick noteClick;
            noteClick = new NoteClick();
            noteClick.setNoteId(noteId);
            noteClick.setUserId(userId);
            noteClick.setSession(userSessionRepository.findById(sessionId).orElse(null));
            noteClick.setCreatedAt(LocalDateTime.now()); 
        
        noteClickRepository.save(noteClick);
            
    }
}
