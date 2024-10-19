package com.uninote.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uninote.backend.service.OwnProfileViewService;

@RestController
@RequestMapping("/own-profile-view")
public class OwnProfileViewController {
    
    @Autowired
    private OwnProfileViewService ownProfileViewService;

    @PostMapping("/{userId}")
    public void saveOwnProfileView(@PathVariable Long userId) {
        ownProfileViewService.recordView(userId);
    }
}
