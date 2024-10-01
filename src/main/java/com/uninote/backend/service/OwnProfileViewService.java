package com.uninote.backend.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uninote.backend.entity.OwnProfileView;
import com.uninote.backend.repository.OwnProfileViewRepository;

@Service
public class OwnProfileViewService {

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private OwnProfileViewRepository ownProfileViewRepository;
    
    public void recordView(Long userId) {
        Long sessionId = userSessionService.findLastSessionForUser(userId);
        OwnProfileView ownProfileView = new OwnProfileView();
        ownProfileView.setSessionId(sessionId);
        ownProfileView.setViewTimestamp(LocalDateTime.now());
        ownProfileView.setUserId(userId);
        ownProfileViewRepository.save(ownProfileView);
    }
}
