package com.uninote.backend.service;

import com.uninote.backend.entity.Badge;
import com.uninote.backend.repository.BadgeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BadgeService {

    @Autowired
    private BadgeRepository badgeRepository;

    public Badge saveBadge(Badge badge) {
        return badgeRepository.save(badge);
    }

    public Badge getBadgeById(Long id) {
        return badgeRepository.findById(id).orElse(null);
    }

    public List<Badge> getAllBadges() {
        return badgeRepository.findAll();
    }

    public void deleteBadge(Long id) {
        badgeRepository.deleteById(id);
    }
}
