package com.uninote.backend.service;

import com.uninote.backend.entity.NoteClick;
import com.uninote.backend.repository.NoteClickRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class NoteClickService {
    @Autowired
    private NoteClickRepository noteClickRepository;

    public NoteClick trackClick(Long noteId, Long userId) {
        NoteClick noteClick;
            noteClick = new NoteClick();
            noteClick.setNoteId(noteId);
            noteClick.setUserId(userId);
            noteClick.setCreatedAt(LocalDateTime.now()); 
        
        return noteClickRepository.save(noteClick);
    }
}
