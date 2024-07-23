package com.uninote.backend.controller;

import com.uninote.backend.entity.UserBadge;
import com.uninote.backend.service.UserBadgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user-badges")
public class UserBadgeController {

    @Autowired
    private UserBadgeService userBadgeService;

    @PostMapping
    public UserBadge createUserBadge(@RequestBody UserBadge userBadge) {
        return userBadgeService.saveUserBadge(userBadge);
    }

    @GetMapping("/{userId}/{badgeId}")
    public UserBadge getUserBadge(@PathVariable Long userId, @PathVariable Long badgeId) {
        return userBadgeService.getUserBadgeById(userId, badgeId);
    }

    @GetMapping
    public List<UserBadge> getAllUserBadges() {
        return userBadgeService.getAllUserBadges();
    }

    @DeleteMapping("/{userId}/{badgeId}")
    public void deleteUserBadge(@PathVariable Long userId, @PathVariable Long badgeId) {
        userBadgeService.deleteUserBadge(userId, badgeId);
    }
}
