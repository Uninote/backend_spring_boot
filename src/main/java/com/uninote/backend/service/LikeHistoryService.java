package com.uninote.backend.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uninote.backend.entity.LikeHistory;
import com.uninote.backend.repository.LikeHistoryRepository;
import com.uninote.backend.repository.UserSessionRepository;


@Service
public class LikeHistoryService {

    @Autowired
    private LikeHistoryRepository likeHistoryRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    public void saveLikeHistory(Long userId, Long noteId, int action, Long sessionId) {
        if (action != 0 && action != 1) {
            throw new IllegalArgumentException("Invalid action. Must be 0 (UNLIKE) or 1 (LIKE).");
        }
        LikeHistory likeHistory = new LikeHistory();
        likeHistory.setUserId(userId);
        likeHistory.setNoteId(noteId);
        likeHistory.setAction(action);  
        likeHistory.setActionTime(LocalDateTime.now());
        
        likeHistory.setSession(userSessionRepository.findById(sessionId).orElse(null));
        likeHistoryRepository.save(likeHistory);
    }
}
