package com.uninote.backend.controller;

import com.uninote.backend.entity.ProfileView;
import com.uninote.backend.entity.User;
import com.uninote.backend.service.ProfileViewService;
import com.uninote.backend.service.UserService;
import com.uninote.backend.service.UserSessionService;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;  
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("profile-views")
public class ProfileViewController {

    @Autowired
    private ProfileViewService profileViewService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserSessionService userSessionService;

    
    @PostMapping("/view/{viewerId}/{profileOwnerId}")
    public ResponseEntity<Void> createProfileView(
            @PathVariable Long viewerId,
            @PathVariable Long profileOwnerId,
            @RequestParam(required = false) Long sessionId) {

        
        
            
            if (sessionId == null || !userSessionService.isSessionValid(sessionId)) {
                sessionId = userSessionService.findLastSessionForUser(viewerId);
                if (sessionId == null) {
                    return ResponseEntity.badRequest().body(null);  
                }
            }

            
           
            
            profileViewService.createProfileView(viewerId, profileOwnerId, sessionId);

            return ResponseEntity.ok().build();
    }
}
