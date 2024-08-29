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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


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
    public  CompletableFuture<NoteView> trackView(Long noteId, Long userId) {
        return CompletableFuture.supplyAsync(() -> {


            Note note = noteRepository.findById(noteId)
                    .orElseThrow(() -> new IllegalArgumentException("Note not found"));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            User noteCreator = note.getUser();
            NoteView noteView;

                noteView = new NoteView();
                noteView.setNoteId(noteId);
                noteView.setUserId(userId);
                noteView.setCreatedAt(LocalDateTime.now());; 
            
                userService.updateUniScore(note.getUser(), 3L);
        
             return noteViewRepository.save(noteView);


        }).exceptionally(ex -> {
            System.err.println("Error occurred in trackView: " + ex.getMessage());
            throw new RuntimeException("Failed to track view", ex);
        });
    }
}
