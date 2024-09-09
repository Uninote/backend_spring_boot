package com.uninote.backend.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uninote.backend.entity.SaveHistory;
import com.uninote.backend.repository.SaveHistoryRepository;
import com.uninote.backend.repository.UserSessionRepository;

@Service
public class SaveHistoryService {

    @Autowired
    private SaveHistoryRepository saveHistoryRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    public void saveSaveHistory(Long userId, Long noteId, int action, Long sessionId) {
        SaveHistory saveHistory = new SaveHistory();
        saveHistory.setUserId(userId);
        saveHistory.setNoteId(noteId);
        saveHistory.setAction(action);  // 1 for 'SAVE', 0 for 'UNSAVE'
        saveHistory.setActionTime(LocalDateTime.now());
       
        saveHistory.setSession(userSessionRepository.findById(sessionId).orElse(null));
        saveHistoryRepository.save(saveHistory);
    }
}