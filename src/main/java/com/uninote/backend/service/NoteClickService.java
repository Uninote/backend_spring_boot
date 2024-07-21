package com.uninote.backend.service;

import com.uninote.backend.entity.NoteClick;
import com.uninote.backend.repository.NoteClickRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NoteClickService {
    @Autowired
    private NoteClickRepository noteClickRepository;

    public NoteClick trackClick(Long noteId, Long userId) {
        Optional<NoteClick> existingClick = noteClickRepository.findByNoteIdAndUserId(noteId, userId);
        NoteClick noteClick;
        if (existingClick.isPresent()) {
            noteClick = existingClick.get();
            noteClick.setClickCount(noteClick.getClickCount() + 1);
        } else {
            noteClick = new NoteClick();
            noteClick.setNoteId(noteId);
            noteClick.setUserId(userId);
            noteClick.setClickCount(1L); 
        }
        return noteClickRepository.save(noteClick);
    }
}
