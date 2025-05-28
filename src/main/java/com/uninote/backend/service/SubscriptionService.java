package com.uninote.backend.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.uninote.backend.entity.*;
import com.uninote.backend.repository.SubscriptionRepository;
import com.uninote.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Value;


import java.time.LocalDateTime;

@Service
public class SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    public Subscription createSubscription(String email,
                                           SubscriptionPlan plan,
                                           SubscriptionDuration duration,
                                           String stripeSubId,
                                           String stripeCustId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = calculateEndDate(start, plan, duration);

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
        subscription.setPlanName(plan.name());
        subscription.setPlan(plan);
        subscription.setDuration(duration);
        subscription.setStartDate(start);
        subscription.setEndDate(end);

        return subscriptionRepository.save(subscription);
    }

    private LocalDateTime calculateEndDate(LocalDateTime start, SubscriptionPlan plan, SubscriptionDuration duration) {
        if (plan == SubscriptionPlan.FREE) {
            return null;
        }

        if (duration == null) {
            throw new IllegalArgumentException("Paid plan must have a duration.");
        }

        if (duration == SubscriptionDuration.ONE_MONTH) {
            return start.plusMonths(1);
        } else if (duration == SubscriptionDuration.ONE_YEAR) {
            return start.plusYears(1);
        } else {
            throw new IllegalArgumentException("Unknown subscription duration: " + duration);
        }
    }
    public boolean hasActiveSubscription(Long id) {
        return userRepository.findById(id)
            .map(subscriptionRepository::existsActiveByUser)
            .orElse(false);
    }

    public void handleStripeWebhook(String customerEmail, String stripeSubscriptionId, String durationStr) {
        try {
            com.stripe.model.Subscription stripeSub = com.stripe.model.Subscription.retrieve(stripeSubscriptionId);
            String stripeCustomerId = stripeSub.getCustomer();

            User user = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.getStripeCustomerId() == null) {
                user.setStripeCustomerId(stripeCustomerId);
                userRepository.save(user);
            } else if (!user.getStripeCustomerId().equals(stripeCustomerId)) {
                throw new SecurityException("Customer ID mismatch.");
            }

            boolean exists = subscriptionRepository.existsByStripeSubscriptionId(stripeSubscriptionId);
            if (exists) {
                return;
            }

            SubscriptionDuration duration = SubscriptionDuration.valueOf(durationStr);
            SubscriptionPlan plan = SubscriptionPlan.BASIC;

            createSubscription(customerEmail, plan, duration, stripeSubscriptionId, stripeCustomerId);

        } catch (com.stripe.exception.InvalidRequestException e) {
            if (e.getStatusCode() == 404) {
                throw new RuntimeException("Stripe subscription not found: " + stripeSubscriptionId);
            }
            throw new RuntimeException("Stripe request error: " + e.getMessage(), e);
        } catch (StripeException e) {
            throw new RuntimeException("Stripe error: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid subscription duration: " + durationStr, e);
        }
    }





}
