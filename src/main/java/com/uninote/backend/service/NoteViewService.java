package com.uninote.backend.service;

import com.uninote.backend.entity.Note;
import com.uninote.backend.entity.NoteView;
import com.uninote.backend.entity.UniscoreIncreaseLog;
import com.uninote.backend.entity.UniscoreIncreaseType;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.NoteRepository;
import com.uninote.backend.repository.NoteViewRepository;
import com.uninote.backend.repository.UniscoreIncreaseLogRepository;
import com.uninote.backend.repository.UniscoreIncreaseTypeRepository;
import com.uninote.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Service
public class NoteViewService {
    @Autowired
    private NoteViewRepository noteViewRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UniscoreIncreaseLogRepository uniscoreIncreaseLogRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UniscoreIncreaseTypeRepository uniscoreIncreaseTypeRepository;

    public  Long trackView(Long noteId, Long userId, Long sessionId) {
        


            Note note = noteRepository.findById(noteId)
                    .orElseThrow(() -> new IllegalArgumentException("Note not found"));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            User noteCreator = note.getUser();
            NoteView noteView;

                noteView = new NoteView();
                noteView.setNoteId(noteId);
                noteView.setUserId(userId);
                noteView.setSessionId(sessionId);
                noteView.setCreatedAt(LocalDateTime.now());; 
            
            CompletableFuture.runAsync(() -> {
                        if (!noteCreator.getId().equals(userId)) {
                                userService.updateUniScore(note.getUser(), 3L);
                        }
                });        
             NoteView nv =  noteViewRepository.save(noteView);
             return nv.getId();


        
    }


    public Long trackViewEnd(Long noteViewId) {
        
        NoteView noteView = noteViewRepository.findById(noteViewId)
                .orElseThrow(() -> new IllegalArgumentException("NoteView not found"));
    
        
        noteView.setViewEndTime(LocalDateTime.now());
    
        
        NoteView updatedNoteView = noteViewRepository.save(noteView);
    
        
        return updatedNoteView.getId();
    }


   
    
}
