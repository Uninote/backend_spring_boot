package com.uninote.backend.controller;

import com.uninote.backend.dto.BadgeDTO;
import com.uninote.backend.dto.UserBadgeDTO;
import com.uninote.backend.dto.UserHasBadgeDTO;
import com.uninote.backend.entity.User;
import com.uninote.backend.interfaceProjection.BadgeProjection;
import com.uninote.backend.repository.UserRepository;
import com.uninote.backend.service.BadgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/badges")
public class BadgeController {

    @Autowired
    private BadgeService badgeService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public BadgeDTO createBadge(@RequestBody BadgeDTO badgeDTO) {
        return badgeService.saveBadge(badgeDTO);
    }   

    @GetMapping("/{id}")
    public BadgeDTO getBadge(@PathVariable Long id) {   
        return badgeService.getBadgeById(id);
    }

    @GetMapping
    public List<BadgeDTO> getAllBadges() {
        return badgeService.getAllBadges();
    }

    @DeleteMapping("/{id}")
    public void deleteBadge(@PathVariable Long id) {
        badgeService.deleteBadge(id);
    }

    

    @PostMapping("/assign")
    public UserBadgeDTO assignBadgeToUser(@RequestBody UserBadgeDTO userBadgeDTO) {
        badgeService.assignBadgeToUser(userBadgeDTO);
        User user = userRepository.findById(userBadgeDTO.getUserId())
                                  .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return userBadgeDTO;
    }

    @GetMapping("/user/{userId}")
    public List<BadgeProjection> getUserBadges(@PathVariable Long userId) {
        return badgeService.getAllBagdesByUser(userId);
    }
}   
