package com.uninote.backend.controller;

import com.uninote.backend.entity.Badge;
import com.uninote.backend.service.BadgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/badges")
public class BadgeController {

    @Autowired
    private BadgeService badgeService;

    @PostMapping
    public Badge createBadge(@RequestBody Badge badge) {
        return badgeService.saveBadge(badge);
    }

    @GetMapping("/{id}")
    public Badge getBadge(@PathVariable Long id) {
        return badgeService.getBadgeById(id);
    }

    @GetMapping
    public List<Badge> getAllBadges() {
        return badgeService.getAllBadges();
    }

    @DeleteMapping("/{id}")
    public void deleteBadge(@PathVariable Long id) {
        badgeService.deleteBadge(id);
    }
}
