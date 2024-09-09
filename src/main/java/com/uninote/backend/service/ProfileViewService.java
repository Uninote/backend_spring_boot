package com.uninote.backend.service;

import com.uninote.backend.entity.ProfileView;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.ProfileViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ProfileViewService {

    @Autowired
    private ProfileViewRepository profileViewRepository;

    @Autowired
    private UserService userService;


    @Async
    public ProfileView saveProfileView(ProfileView profileView) {
        return profileViewRepository.save(profileView);
    }

    @Async
    public ResponseEntity<Long> createProfileView(Long viewerId, Long profileOwnerId, Long sessionId) {

        

        User viewer = userService.findById(viewerId);
        User profileOwner = userService.findById(profileOwnerId);

        ProfileView profileView = new ProfileView();
        profileView.setViewer(viewer);
        profileView.setProfileOwner(profileOwner);
        profileView.setSessionId(sessionId);

        ProfileView savedProfileView = profileViewRepository.save(profileView);

        return ResponseEntity.ok(savedProfileView.getViewId());
    }
}
