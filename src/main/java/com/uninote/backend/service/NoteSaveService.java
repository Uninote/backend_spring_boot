package com.uninote.backend.service;

import com.uninote.backend.entity.NoteSave;
import com.uninote.backend.entity.NoteSaveId;
import com.uninote.backend.entity.UniscoreIncreaseLog;
import com.uninote.backend.entity.UniscoreIncreaseType;
import com.uninote.backend.entity.Note;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.NoteSaveRepository;
import com.uninote.backend.repository.UniscoreIncreaseLogRepository;
import com.uninote.backend.repository.UniscoreIncreaseTypeRepository;
import com.uninote.backend.repository.NoteRepository;
import com.uninote.backend.repository.UserRepository;
import com.uninote.backend.repository.UserSessionRepository;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NoteSaveService {
    @Autowired
    private NoteSaveRepository noteSaveRepository;

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

    @Autowired
    private SaveHistoryService saveHistoryService;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Transactional
    public void saveNote(Long noteId, Long userId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        User noteCreator = note.getUser();

        Optional<NoteSave> noteSave = noteSaveRepository.findByNoteIdAndUserId(noteId, userId);
        if (!noteSave.isPresent()) {
            NoteSave newNoteSave = new NoteSave(noteId, userId);
            noteSaveRepository.save(newNoteSave);
            userService.updateUniScore(noteCreator, 2l);
        } else if (!noteSave.get().getIsActive()) {
            noteSave.get().setIsActive(true);
            noteSaveRepository.save(noteSave.get());

           
        }
    }

    @Transactional
    public void unsaveNote(Long noteId, Long userId) {
        Optional<NoteSave> noteSave = noteSaveRepository.findByNoteIdAndUserId(noteId, userId);
        if (noteSave.isPresent() && noteSave.get().getIsActive()) {
            noteSave.get().setIsActive(false);
            noteSaveRepository.save(noteSave.get());

            
        }
    }


    @Async
    @Transactional
    public void toggleSave(Long noteId, Long userId, Long sessionId) {
        // Fetch the note
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));

        // Fetch the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        User noteCreator = note.getUser();
        Optional<NoteSave> existingSave = noteSaveRepository.findByNoteIdAndUserId(noteId, userId);
        NoteSave noteSave = existingSave.orElse(null);

        if (noteSave == null) {
            
            
            noteSave = new NoteSave(noteId, userId);
            noteSave.setIsActive(true);
            noteSaveRepository.save(noteSave);
            if (noteCreator.getId() != userId) {
                userService.updateUniScore(noteCreator, 2l);
            }
            saveHistoryService.saveSaveHistory(userId, noteId, 1, sessionId);  
        } else if (noteSave.getIsActive()) {
            noteSave.setIsActive(false);
            noteSaveRepository.save(noteSave);

            saveHistoryService.saveSaveHistory(userId, noteId, 0, sessionId); 
        } else {
            
            noteSave.setIsActive(true);
            noteSaveRepository.save(noteSave);

            saveHistoryService.saveSaveHistory(userId, noteId, 1, sessionId);  
        }
}

}
