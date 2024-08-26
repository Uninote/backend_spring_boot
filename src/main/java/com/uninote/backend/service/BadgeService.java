package com.uninote.backend.service;

import com.uninote.backend.controller.BadgeWebSocketController;
import com.uninote.backend.converter.DTOToEntityConverter;
import com.uninote.backend.converter.EntityToDTOConverter;
import com.uninote.backend.dto.BadgeDTO;
import com.uninote.backend.dto.UserBadgeDTO;
import com.uninote.backend.dto.UserHasBadgeDTO;
import com.uninote.backend.entity.Badge;
import com.uninote.backend.entity.BadgeNotification;
import com.uninote.backend.entity.User;
import com.uninote.backend.entity.UserBadge;
import com.uninote.backend.entity.UserBadgeId;
import com.uninote.backend.interfaceProjection.BadgeProjection;
import com.uninote.backend.repository.BadgeNotificationRepository;
import com.uninote.backend.repository.BadgeRepository;
import com.uninote.backend.repository.BadgeTypeRepository;
import com.uninote.backend.repository.InviteRepository;
import com.uninote.backend.repository.NoteRepository;
import com.uninote.backend.repository.UserBadgeRepository;
import com.uninote.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

@Service
public class BadgeService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BadgeWebSocketController badgeWebSocketController;

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private BadgeTypeRepository badgeTypeRepository;

    @Autowired
    private UserBadgeRepository userBadgeRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private InviteRepository inviteRepository;

    @Autowired
    private BadgeNotificationRepository badgeNotificationRepository;


    private static final Logger logger = LoggerFactory.getLogger(BadgeService.class);


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
    public void assignBadgeToUser(UserBadgeDTO userBadgeDTO) {
        User user = userRepository.findById(userBadgeDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Badge badge = badgeRepository.findById(userBadgeDTO.getBadgeId())
                .orElseThrow(() -> new IllegalArgumentException("Badge not found"));

        if (!meetsRequirement(user, badge)) {
            throw new IllegalArgumentException("User does not meet the requirements for this badge.");
        }

        UserBadge userBadge = new UserBadge();
        userBadge.setUser(user);
        userBadge.setBadge(badge);
        userBadge.setAwardedAt(LocalDateTime.now());
        
        logger.debug("Badge assigned: {}", userBadgeDTO);
        BadgeNotification badgeNotification = new BadgeNotification(
                badge.getId(),
                user.getId(),
                LocalDateTime.now()
        );
        badgeNotificationRepository.save(badgeNotification);

        badgeWebSocketController.sendBadgeNotification(user.getId(),badgeNotification.getId(), badge.getId());    }


    @Transactional
    public void assignBadgeToUser(Long userId, Long badgeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Badge badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new IllegalArgumentException("Badge not found"));

        if (!meetsRequirement(user, badge)) {
            throw new IllegalArgumentException("User does not meet the requirements for this badge.");
        }

        UserBadge userBadge = new UserBadge();
        userBadge.setUser(user);
        userBadge.setBadge(badge);
        userBadge.setAwardedAt(LocalDateTime.now());
        
        BadgeNotification badgeNotification = new BadgeNotification(
                badge.getId(),
                user.getId(),
                LocalDateTime.now()
        );
        badgeNotificationRepository.save(badgeNotification);

        badgeWebSocketController.sendBadgeNotification(user.getId(),  badgeNotification.getId(),badge.getId());
    }

    public void deliverPendingNotifications(Long userId) {
        List<BadgeNotification> undeliveredNotifications = badgeNotificationRepository.findByUserIdAndDeliveredFalse(userId);

        for (BadgeNotification notification : undeliveredNotifications) {
            badgeWebSocketController.sendBadgeNotification(notification.getUserId(), notification.getId(), notification.getBadgeId());
            
        }
    }

        @Transactional
        public List<BadgeDTO> getUserBadges(Long userId) { 
            User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User Not found"));
            List<UserBadge> userBadges = userBadgeRepository.findByUser(user);
            return userBadges.stream().map(EntityToDTOConverter::convertUserBadgeToBadgeDTO).collect(Collectors.toList());
        }

        //add extra field 0 or 1 depending on if user has badge
        public List<BadgeProjection> getAllBagdesByUser(Long userId) {
            return userBadgeRepository.findAllBadgesByUserId(userId);
         }
       /*  @Transactional
        public List<UserHasBadgeDTO> getAllBagdesByUser(Long userId) {
            User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User Not found"));
            List<Badge> allBadges = badgeRepository.findAll();
            List<Badge> userBadges = userBadgeRepository.findBadgesByUserId(userId);
            Set<Long> userBadgeIds = userBadges.stream()
            .map(Badge::getId)
            .collect(Collectors.toSet());
             return allBadges.stream()
            .map(badge -> new UserHasBadgeDTO(
                badge.getId(),
                badge.getName(),
                badge.getDescription(),
                badge.getImageUrl(),
                userBadgeIds.contains(badge.getId()), 
                userId,
                badge.getType().getName()))
            .collect(Collectors.toList());

        } */
        private boolean meetsRequirement(User user, Badge badge) {
            switch (badge.getType().getId().intValue()) {
                case 1:     
                    return noteRepository.countByUserId(user.getId()) >= badge.getRequirement();
                case 2:         
                    return inviteRepository.countByUserIdAndInviteeIsNotNull(user.getId()) >= badge.getRequirement();
                case 3: 
                    
                    return user.getUniscore() >=badge.getRequirement();
                case 4:
                    return user.getStreak() >=badge.getRequirement();
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
