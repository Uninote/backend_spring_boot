package com.uninote.backend.service;

import com.uninote.backend.converter.DTOToEntityConverter;
import com.uninote.backend.converter.EntityToDTOConverter;
import com.uninote.backend.dto.BadgeDTO;
import com.uninote.backend.dto.UserBadgeDTO;
import com.uninote.backend.entity.Badge;
import com.uninote.backend.entity.User;
import com.uninote.backend.entity.UserBadge;
import com.uninote.backend.entity.UserBadgeId;
import com.uninote.backend.repository.BadgeRepository;
import com.uninote.backend.repository.BadgeTypeRepository;
import com.uninote.backend.repository.NoteRepository;
import com.uninote.backend.repository.UserBadgeRepository;
import com.uninote.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

@Service
public class BadgeService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private BadgeTypeRepository badgeTypeRepository;

    @Autowired
    private UserBadgeRepository userBadgeRepository;

    @Autowired
    private NoteRepository noteRepository;

    public BadgeDTO saveBadge(BadgeDTO badgeDTO) {
        Badge badge = DTOToEntityConverter.convertDTOToBadge(badgeDTO,badgeTypeRepository);
        Badge savedBadge = badgeRepository.save(badge); 
        return EntityToDTOConverter.convertBadgeToBadgeDTO(savedBadge);
    }

    public BadgeDTO getBadgeById(Long id) {
        return badgeRepository.findById(id)
                .map(EntityToDTOConverter::convertBadgeToBadgeDTO)
                .orElse(null);
    }

    public List<BadgeDTO> getAllBadges() {
        return badgeRepository.findAll()
                .stream()
                .map(EntityToDTOConverter::convertBadgeToBadgeDTO)
                .collect(Collectors.toList());
    }

    public void deleteBadge(Long id) {
        badgeRepository.deleteById(id);
    }

    @Transactional
    public UserBadgeDTO assignBadgeToUser(UserBadgeDTO userBadgeDTO) {
        UserBadge userBadge = new UserBadge();  
        User user = userRepository.findById(userBadgeDTO.getUserId()).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Badge badge = badgeRepository.findById(userBadgeDTO.getUserId()).orElseThrow(() -> new IllegalArgumentException("Badge not found"));
        userBadge.setUser(user);
        userBadge.setBadge(badge);
        userBadge.setAwardedAt(LocalDateTime.now());

        userBadgeRepository.save(userBadge);

        
        return userBadgeDTO;
    }


    @Transactional
    public void assignBadgeToUser(Long userId, Long badgeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Badge badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new IllegalArgumentException("Badge not found"));

        // Check requirements based on badge type and requirement
        if (!meetsRequirement(user, badge)) {
            throw new IllegalArgumentException("User does not meet the requirements for this badge.");
        }

        UserBadge userBadge = new UserBadge();
        userBadge.setUser(user);
        userBadge.setBadge(badge);
        userBadge.setAwardedAt(LocalDateTime.now());

        userBadgeRepository.save(userBadge);
    }

        @Transactional
        public List<BadgeDTO> getUserBadges(Long userId) { 
            User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User Not found"));
            List<UserBadge> userBadges = userBadgeRepository.findByUser(user);
            return userBadges.stream().map(EntityToDTOConverter::convertUserBadgeToBadgeDTO).collect(Collectors.toList());
        }

        private boolean meetsRequirement(User user, Badge badge) {
            switch (badge.getType().getId().intValue()) {
                case 1:     
                return noteRepository.countByUserId(user.getId()) >= badge.getRequirement();
                case 2: 
                    return false;
                case 3: 
                    return false;
                default:
                    return false;
            }
        }

        public void checkBadgesForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<Badge> badges = badgeRepository.findAll();
        for (Badge badge : badges) {
            if (meetsRequirement(user, badge)) {
                if (!userBadgeRepository.existsById(new UserBadgeId(user.getId(), badge.getId()))) {
                    assignBadgeToUser(user.getId(), badge.getId());
                }
            }
        }
    }
}
