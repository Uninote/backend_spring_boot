package com.uninote.backend.service;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.uninote.backend.entity.Subscription;
import com.uninote.backend.entity.SubscriptionDuration;
import com.uninote.backend.entity.SubscriptionPlan;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.SubscriptionRepository;
import com.uninote.backend.repository.UserRepository;

@Service
public class SubscriptionService {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    private void initStripe() {
        if (stripeSecretKey != null && !stripeSecretKey.isBlank()) {
            Stripe.apiKey = stripeSecretKey;
            System.out.println("Stripe API key initialized in SubscriptionService");
        } else {
            throw new IllegalStateException("Stripe secret key is missing!");
        }
    }

    public Subscription createSubscription(String email,
            SubscriptionPlan plan,
            SubscriptionDuration duration,
            String stripeSubId,
            String stripeCustId) {

        logger.info("Creating subscription for user email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        logger.debug("User found: id={}, name={}, email={}", user.getId(), user.getName(), user.getEmail());

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = calculateEndDate(start, plan, duration);
        logger.debug("Subscription period: start={}, end={}", start, end);

        // Use date-based overlap checking to allow subscriptions to start on the same day another ends
        boolean overlapExists = subscriptionRepository
                .existsOverlappingByUserAndDateRange(user, start, end);

        if (overlapExists) {
            logger.warn("Overlapping subscription exists for user: {}", user.getEmail());
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

        logger.debug("Subscription object: userId={}, planName={}, duration={}, stripeSubId={}, stripeCustId={}",
                user.getId(), plan.name(), duration.name(), stripeSubId, stripeCustId);

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
        } else if (duration == SubscriptionDuration.ONE_WEEK) {
            return start.plusWeeks(1);
        } else if (duration == SubscriptionDuration.THREE_DAYS) {
            return start.plusDays(3);
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
            logger.info("Received Stripe webhook: email={}, subscriptionId={}, duration={}", customerEmail,
                    stripeSubscriptionId, durationStr);

            com.stripe.model.Subscription stripeSub = com.stripe.model.Subscription.retrieve(stripeSubscriptionId);
            logger.debug("Received sub");

            String stripeCustomerId = stripeSub.getCustomer();
            logger.debug("Retrieved Stripe customer ID: {}", stripeCustomerId);

            User user = userRepository.findByEmail(customerEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            logger.debug("Found user: id={}, email={}, name={}", user.getId(), user.getEmail(), user.getName());

            if (user.getName() == null) {
                logger.warn("User '{}' has null name. Setting default name.", user.getEmail());
                user.setName("Unnamed User"); // or however you want to handle it
                userRepository.save(user);
            }

            if (user.getStripeCustomerId() == null) {
                user.setStripeCustomerId(stripeCustomerId);
                userRepository.save(user);
                logger.info("Set Stripe customer ID for user: {}", user.getEmail());
            } else if (!user.getStripeCustomerId().equals(stripeCustomerId)) {
                throw new SecurityException("Customer ID mismatch.");
            }

            boolean exists = subscriptionRepository.existsByStripeSubscriptionId(stripeSubscriptionId);
            if (exists) {
                logger.info("Subscription already exists for Stripe ID: {}", stripeSubscriptionId);
                return;
            }

            SubscriptionDuration duration = SubscriptionDuration.valueOf(durationStr);
            SubscriptionPlan plan = SubscriptionPlan.BASIC;

            createSubscription(customerEmail, plan, duration, stripeSubscriptionId, stripeCustomerId);

        } catch (com.stripe.exception.InvalidRequestException e) {
            logger.error("Stripe InvalidRequestException: {}", e.getMessage(), e);
            if (e.getStatusCode() == 404) {
                throw new RuntimeException("Stripe subscription not found: " + stripeSubscriptionId);
            }
            throw new RuntimeException("Stripe request error: " + e.getMessage(), e);
        } catch (StripeException e) {
            logger.error("Stripe exception: {}", e.getMessage(), e);
            throw new RuntimeException("Stripe error: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid subscription duration: {}", durationStr, e);
            throw new RuntimeException("Invalid subscription duration: " + durationStr, e);
        }
    }

    @Transactional
    public Subscription createFreeTrialSubscription(Long userId, SubscriptionPlan plan, JsonNode metadata) {

        logger.info("Creating free trial subscription for user id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = calculateEndDate(start, plan, SubscriptionDuration.THREE_DAYS);
        
        // Use date-based overlap checking to allow subscriptions to start on the same day another ends
        boolean overlapExists = subscriptionRepository
                .existsOverlappingByUserAndDateRange(user, start, end);        
        if (overlapExists) {
            throw new IllegalStateException("User already has an active subscription.");
        }

        logger.debug("User found: id={}, name={}, email={}", user.getId(), user.getName(), user.getEmail());

        user.setMetadata(metadata);
        userRepository.save(user);

        

        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setPlan(plan);
        subscription.setDuration(SubscriptionDuration.THREE_DAYS);
        subscription.setStartDate(start);
        subscription.setEndDate(end);
        subscription.setStripeSubscriptionId(null);
        subscription.setStripeCustomerId(null);
        subscription.setStatus("active");
        Subscription savedSub = subscriptionRepository.save(subscription);

        logger.info("Free trial subscription created: id={}, start={}, end={}",
                savedSub.getId(), start, end);

        return savedSub;
    }

    public boolean existsByUserIdAndDuration(Long id, SubscriptionDuration threeDays) {
        return subscriptionRepository.existsByUser_IdAndDuration(id, threeDays);
    }

    public Optional<Subscription> findByUser_IdAndDuration(Long userId, SubscriptionDuration duration) {
        return subscriptionRepository.findByUser_IdAndDuration(userId, duration);
    }

    /**
     * Finds the active subscription for a user (status = 'active').
     */
    public Optional<Subscription> findActiveSubscriptionByUserId(Long userId) {
        return subscriptionRepository.findByUser_IdAndStatus(userId, "active");
    }

    /**
     * Finds the latest active (paid) subscription for a user.
     * "Active" means:
     *   - status = 'active'
     *   - startDate <= now
     *   - (endDate is null OR endDate >= now)
     *   - plan != FREE
     * Returns the most recent by startDate.
     */
    public Optional<Subscription> findLatestActiveSubscriptionByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        return subscriptionRepository.findLatestActiveByUser(user, LocalDateTime.now(), SubscriptionPlan.FREE);
    }

    /**
     * Saves the given Subscription entity.
     */
    public Subscription saveSubscription(Subscription subscription) {
        return subscriptionRepository.save(subscription);
    }
}
