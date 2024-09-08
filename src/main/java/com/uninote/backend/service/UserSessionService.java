package com.uninote.backend.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uninote.backend.entity.UserSession;
import com.uninote.backend.repository.UserSessionRepository;

@Service
public class UserSessionService {

    @Autowired
    private UserSessionRepository userSessionRepository;

    public Long startSession(Long userId) {
    UserSession session = new UserSession();
    session.setUserId(userId);
    session.setLoginTime(LocalDateTime.now());
    UserSession savedSession =  userSessionRepository.save(session);
    return savedSession.getSessionId();
}

public void endSession(Long sessionId) {
    UserSession session = userSessionRepository.findById(sessionId)
        .orElseThrow(() -> new RuntimeException("Session not found"));
    session.setLogoutTime(LocalDateTime.now());
    session.setSessionStatus(false);  
    userSessionRepository.save(session);
}


public Long findLastSessionForUser(Long userId) {
    return userSessionRepository.findLastActiveSessionIdByUserId(userId)
            .orElse(null);  
}

public boolean isSessionValid(Long sessionId) {
    return userSessionRepository.existsById(sessionId);
}


}
