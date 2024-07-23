package com.uninote.backend.service;

import com.uninote.backend.entity.UserBadge;
import com.uninote.backend.entity.UserBadgeId;
import com.uninote.backend.repository.UserBadgeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserBadgeService {

    @Autowired
    private UserBadgeRepository userBadgeRepository;

    public UserBadge saveUserBadge(UserBadge userBadge) {
        return userBadgeRepository.save(userBadge);
    }   

    public UserBadge getUserBadgeById(Long userId, Long badgeId) {
        return userBadgeRepository.findById(new UserBadgeId(userId, badgeId)).orElse(null);
    }

    public List<UserBadge> getAllUserBadges() {
        return userBadgeRepository.findAll();
    }

    public void deleteUserBadge(Long userId, Long badgeId) {
        userBadgeRepository.deleteById(new UserBadgeId(userId, badgeId));
    }
}
