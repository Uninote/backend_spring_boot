package com.uninote.backend.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uninote.backend.entity.Subscription;
import com.uninote.backend.entity.SubscriptionDuration;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.SubscriptionRepository;
import com.uninote.backend.repository.UserRepository;

@Service
public class SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    public Subscription createSubscription(String email, SubscriptionDuration duration, String stripeSubId, String stripeCustId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = calculateEndDate(start, duration);

        boolean overlapExists = subscriptionRepository
                .existsByUserAndStartDateBeforeAndEndDateAfter(user, end, start);

        if (overlapExists) {
            throw new IllegalStateException("User already has an overlapping active subscription.");
        }

        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setStripeSubscriptionId(stripeSubId);
        subscription.setStripeCustomerId(stripeCustId);
        subscription.setStatus("active");
        subscription.setPlanName(duration.name()); // Optional: map to actual product name
        subscription.setStartDate(start);
        subscription.setEndDate(end);
        subscription.setDurationType(duration);

        return subscriptionRepository.save(subscription);
    }

   private LocalDateTime calculateEndDate(LocalDateTime start, SubscriptionDuration duration) {
        if (duration == SubscriptionDuration.ONE_DAY) {
            return start.plusDays(1);
        } else if (duration == SubscriptionDuration.ONE_MONTH) {
            return start.plusMonths(1);
        } else if (duration == SubscriptionDuration.ONE_YEAR) {
            return start.plusYears(1);
        } else {
            throw new IllegalArgumentException("Unknown subscription duration: " + duration);
        }
    }

}
