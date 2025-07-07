package com.uninote.backend.service;

import com.uninote.backend.dto.UserLimitsDTO;
import com.uninote.backend.entity.*;
import com.uninote.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Service
public class UsageLimitService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private MessageRepository messageRepository;



    private Date getStartOfDay() {
        return java.sql.Timestamp.valueOf(LocalDate.now().atStartOfDay());
    }

    private Date getEndOfDay() {
        return java.sql.Timestamp.valueOf(LocalDate.now().plusDays(1).atStartOfDay());
    }


    public void checkDailyChatLimit(User user) {
        SubscriptionPlan plan = getCurrentPlan(user);
        int limit = PlanLimits.getDailyChatLimit(plan);
        if (limit == Integer.MAX_VALUE) return;

        long count = chatRepository.countByUserAndCreatedAtBetween(user, getStartOfDay(), getEndOfDay());
        if (count >= limit) {
            throw new AccessDeniedException("You’ve reached your daily chat limit.");
        }
    }

    public void checkDailySpaceLimit(User user) {
        SubscriptionPlan plan = getCurrentPlan(user);
        int limit = PlanLimits.getMaxSpaces(plan);
        if (limit == Integer.MAX_VALUE) return;

        long count = spaceRepository.countByUserAndCreatedAtBetween(user, getStartOfDay(), getEndOfDay());
        if (count >= limit) {
            throw new AccessDeniedException("You’ve reached your daily space limit.");
        }
    }

    public void checkDailyMessageLimit(User user, Chat chat) {
        SubscriptionPlan plan = getCurrentPlan(user);
        int limit = PlanLimits.getMaxMessagesPerChat(plan);
        if (limit == Integer.MAX_VALUE) return;

        long count = messageRepository.countByChat_UserAndCreatedAtBetween(user, getStartOfDay(), getEndOfDay());
        if (count >= limit) {
            throw new AccessDeniedException("You’ve reached your daily message limit for this chat.");
        }
    }

    private SubscriptionPlan getCurrentPlan(User user) {
        return subscriptionRepository.findLatestActiveByUser(user, LocalDateTime.now(), SubscriptionPlan.FREE)
                .map(Subscription::getPlan)
                .orElse(SubscriptionPlan.FREE);
    }

    public UserLimitsDTO getUserLimits(User user) {
        SubscriptionPlan plan = getCurrentPlan(user);
        
        // Get daily chat usage
        long dailyChatUsed = chatRepository.countByUserAndCreatedAtBetween(user, getStartOfDay(), getEndOfDay());
        int dailyChatLimit = PlanLimits.getDailyChatLimit(plan);
        
        // Get daily spaces usage
        long dailySpaceUsed = spaceRepository.countByUserAndCreatedAtBetween(user, getStartOfDay(), getEndOfDay());
        int dailySpaceLimit = PlanLimits.getMaxSpaces(plan);
        
        // Get daily messages usage
        long dailyMessageUsed = messageRepository.countByChat_UserAndCreatedAtBetween(user, getStartOfDay(), getEndOfDay());
        int dailyMessageLimit = PlanLimits.getMaxMessagesPerChat(plan);
        
        return new UserLimitsDTO(
            plan,
            dailyChatLimit, (int) dailyChatUsed,
            dailySpaceLimit, (int) dailySpaceUsed,
            dailyMessageLimit, (int) dailyMessageUsed
        );
    }

}
