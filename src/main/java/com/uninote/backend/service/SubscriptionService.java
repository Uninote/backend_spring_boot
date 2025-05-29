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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

import javax.annotation.PostConstruct;

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

        boolean overlapExists = subscriptionRepository
                .existsByUserAndStartDateBeforeAndEndDateAfter(user, end, start);

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
            logger.info("Received Stripe webhook: email={}, subscriptionId={}, duration={}", customerEmail, stripeSubscriptionId, durationStr);
            com.stripe.model.Subscription stripeSub = null;
            try {
                stripeSub = com.stripe.model.Subscription.retrieve(stripeSubscriptionId);
            } catch (com.stripe.exception.StripeException e) {
                
                logger.error("Invalid request to Stripe: {}", e.getMessage());
                throw new RuntimeException("Stripe subscription not found: " + stripeSubscriptionId);
            }
            logger.error("Invalid request to Stripe: new subscription");

            if (stripeSub == null) {
                throw new RuntimeException("Stripe subscription is null.");
            }         
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

        } catch (IllegalArgumentException e) {
            logger.error("Invalid subscription duration: {}", durationStr, e);
            throw new RuntimeException("Invalid subscription duration: " + durationStr, e);
        }
    }






}
