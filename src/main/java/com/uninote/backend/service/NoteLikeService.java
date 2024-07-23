package com.uninote.backend.service;

import com.uninote.backend.entity.*;
import com.uninote.backend.repository.*;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NoteLikeService {

    @Autowired
    private NoteLikeRepository noteLikeRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UniscoreIncreaseTypeRepository uniscoreIncreaseTypeRepository;

    @Autowired
    private UniscoreIncreaseLogRepository uniscoreIncreaseLogRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public void likeNote(Long noteId, Long userId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        User noteCreator = note.getUser();

        Optional<NoteLike> checknoteLike = noteLikeRepository.findByNoteIdAndUserId(noteId, userId);
        NoteLike noteLike = checknoteLike.orElse(null);
        if (noteLike == null) {
            NoteLikeId noteLikeId = new NoteLikeId(noteId, userId);
            noteLike = new NoteLike(noteLikeId, note, user);
            noteLike.setActive(true);
            noteLikeRepository.save(noteLike); 

            userService.updateUniScore(noteCreator, 1L);
            
        } else if (!noteLike.isActive()) {
            
            noteLike.setActive(true);

            
            
        }
    }

    @Transactional
    public void unlikeNote(Long noteId, Long userId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));

        User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User Note found"));
        Optional<NoteLike> checknoteLike = noteLikeRepository.findByNoteIdAndUserId(noteId, userId);
        NoteLike noteLike = checknoteLike.orElse(null);
        if (noteLike != null && noteLike.isActive()) {
           
            noteLike.setActive(false);


            
        }
    }
}
