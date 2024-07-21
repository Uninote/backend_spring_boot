package com.uninote.backend.service;

import com.uninote.backend.entity.NoteSave;
import com.uninote.backend.entity.UniscoreIncreaseLog;
import com.uninote.backend.entity.UniscoreIncreaseType;
import com.uninote.backend.entity.Note;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.NoteSaveRepository;
import com.uninote.backend.repository.UniscoreIncreaseLogRepository;
import com.uninote.backend.repository.UniscoreIncreaseTypeRepository;
import com.uninote.backend.repository.NoteRepository;
import com.uninote.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Transactional
    public void saveNote(Long noteId, Long userId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        User noteCreator = note.getUser();

        NoteSave noteSave = noteSaveRepository.findByNoteIdAndUserId(noteId, userId);
        if (noteSave == null) {
            noteSave = new NoteSave(noteId, userId);
            noteSaveRepository.save(noteSave);

            UniscoreIncreaseType saveIncreaseType = uniscoreIncreaseTypeRepository.findById(2L) 
                    .orElseThrow(() -> new IllegalArgumentException("Increase type not found"));
            noteCreator.setUniscore(user.getUniscore() + saveIncreaseType.getIncreaseAmount());

            UniscoreIncreaseLog increaseLog = new UniscoreIncreaseLog(noteCreator, saveIncreaseType);
            uniscoreIncreaseLogRepository.save(increaseLog);
        } else if (!noteSave.getIsActive()) {
            noteSave.setIsActive(true);
            noteSaveRepository.save(noteSave);

           
        }
    }

    @Transactional
    public void unsaveNote(Long noteId, Long userId) {
        NoteSave noteSave = noteSaveRepository.findByNoteIdAndUserId(noteId, userId);
        if (noteSave != null && noteSave.getIsActive()) {
            noteSave.setIsActive(false);
            noteSaveRepository.save(noteSave);

            
        }
    }
}
