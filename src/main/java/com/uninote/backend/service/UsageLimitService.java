package com.uninote.backend.service;

import com.uninote.backend.entity.*;
import com.uninote.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    private LocalDateTime getStartOfDay() {
        return LocalDate.now().atStartOfDay();
    }

    private LocalDateTime getEndOfDay() {
        return getStartOfDay().plusDays(1);
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
        return subscriptionRepository.findActiveByUser(user)
                .map(Subscription::getPlan)
                .orElse(SubscriptionPlan.FREE);
    }
}
