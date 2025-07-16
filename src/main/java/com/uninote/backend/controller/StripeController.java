package com.uninote.backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uninote.backend.entity.SubscriptionDuration;
import com.uninote.backend.entity.SubscriptionPlan;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.UserRepository;
import com.uninote.backend.service.StripeService;
import com.uninote.backend.config.security.FirebaseAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;


@RestController
@RequestMapping("/api/stripe")
public class StripeController {

    private static final Logger logger = LoggerFactory.getLogger(StripeController.class);
    private final StripeService stripeService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    public StripeController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<?> createCheckoutSession(@RequestBody CreateCheckoutSessionRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authorization token missing or invalid."));
        }
        FirebaseAuthentication firebaseAuth = (FirebaseAuthentication) authentication;
        String userUid = firebaseAuth.getUid();

        User user = userRepository.findByFirebaseUid(userUid)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user UID: " + userUid));
        try {
            String url;
            try {
                url = stripeService.createCheckoutSession(
                        user.getId(),
                        request.getPlan(),
                        request.getDuration(),
                        request.getSuccessUrl(),
                        request.getCancelUrl()
                );
            } catch (com.stripe.exception.StripeException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Stripe error: " + e.getMessage()));
            }
            return ResponseEntity.ok(Map.of("url", url));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

   @PostMapping("/cancel-renewal")
    public ResponseEntity<?> cancelRenewal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>("Authorization token missing or invalid.", HttpStatus.UNAUTHORIZED);
        }
        FirebaseAuthentication firebaseAuth = (FirebaseAuthentication) authentication;
        String userUid = firebaseAuth.getUid();
        User user = userRepository.findByFirebaseUid(userUid)
                .orElseThrow(() -> new RuntimeException("User not found for Firebase UID: " + userUid));
        try {
            stripeService.cancelStripeSubscriptionRenewal(user.getId());
            return ResponseEntity.ok("Subscription renewal cancelled successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error cancelling renewal: " + e.getMessage());
        }
    }

    @GetMapping("/subscription-info")
    public ResponseEntity<?> getSubscriptionInfo() {
        logger.info("[getSubscriptionInfo] Entry");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("[getSubscriptionInfo] Unauthorized access attempt");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authorization token missing or invalid."));
        }
        FirebaseAuthentication firebaseAuth = (FirebaseAuthentication) authentication;
        String userUid = firebaseAuth.getUid();
        logger.debug("[getSubscriptionInfo] userUid={}", userUid);
        User user = userRepository.findByFirebaseUid(userUid)
                .orElse(null);
        if (user == null) {
            logger.error("[getSubscriptionInfo] No user found for Firebase UID: {}", userUid);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found for Firebase UID: " + userUid));
        }
        logger.debug("[getSubscriptionInfo] userId={}, email={}", user.getId(), user.getEmail());
        try {
            // Try to get the latest active subscription entity
            com.uninote.backend.entity.Subscription latestActiveSub = null;
            String planType = "FREE";
            try {
                java.util.Optional<com.uninote.backend.entity.Subscription> opt = stripeService.getLatestActiveSubscriptionEntity(user.getId());
                if (opt.isPresent()) {
                    latestActiveSub = opt.get();
                    if (latestActiveSub.getPlan() != null) {
                        planType = latestActiveSub.getPlan().name();
                    }
                }
            } catch (Exception e) {
                logger.warn("[getSubscriptionInfo] Could not get latest active subscription: {}", e.getMessage());
            }
            // If no active subscription, return plan: FREE and all other fields null/empty
            if (latestActiveSub == null) {
                Map<String, Object> info = new java.util.HashMap<>();
                info.put("plan", planType);
                info.put("id", "");
                info.put("status", "");
                info.put("current_period_end", null);
                info.put("cancel_at_period_end", null);
                info.put("canceled_at", null);
                info.put("price_id", "");
                logger.info("[getSubscriptionInfo] No active subscription, returning plan: {}", planType);
                return ResponseEntity.ok(info);
            }
            com.stripe.model.Subscription sub = stripeService.getStripeSubscriptionInfo(user.getId());
            logger.debug("[getSubscriptionInfo] Stripe subscription object: {}", sub);
            if (sub == null) {
                logger.error("[getSubscriptionInfo] Stripe subscription is null for userId={}", user.getId());
                Map<String, Object> info = new java.util.HashMap<>();
                info.put("plan", planType);
                info.put("id", "");
                info.put("status", "");
                info.put("current_period_end", null);
                info.put("cancel_at_period_end", null);
                info.put("canceled_at", null);
                info.put("price_id", "");
                logger.info("[getSubscriptionInfo] Stripe subscription null, returning plan: {}", planType);
                return ResponseEntity.ok(info);
            }
            // Extract current_period_end from first item if present
            Object currentPeriodEnd = null;
            try {
                if (sub.getItems() != null && sub.getItems().getData() != null && !sub.getItems().getData().isEmpty()) {
                    com.stripe.model.SubscriptionItem item = sub.getItems().getData().get(0);
                    Object value = item.getClass().getMethod("getCurrentPeriodEnd").invoke(item);
                    if (value instanceof Number) {
                        long epochSeconds = ((Number) value).longValue();
                        currentPeriodEnd = Instant.ofEpochSecond(epochSeconds)
                            .atOffset(ZoneOffset.UTC)
                            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                        logger.debug("[getSubscriptionInfo] Extracted and converted currentPeriodEnd from item: {} -> {}", value, currentPeriodEnd);
                    } else {
                        currentPeriodEnd = value;
                        logger.debug("[getSubscriptionInfo] currentPeriodEnd in item is not a number: {}", value);
                    }
                } else {
                    logger.debug("[getSubscriptionInfo] No items or empty items list in subscription.");
                }
            } catch (Exception e) {
                logger.debug("[getSubscriptionInfo] Could not get currentPeriodEnd from item via reflection: {}", e.getMessage());
            }
            String priceId = null;
            if (sub.getItems() != null && sub.getItems().getData() != null && !sub.getItems().getData().isEmpty() && sub.getItems().getData().get(0) != null && sub.getItems().getData().get(0).getPrice() != null) {
                priceId = sub.getItems().getData().get(0).getPrice().getId();
            } else {
                logger.warn("[getSubscriptionInfo] Could not determine priceId for userId={}", user.getId());
            }
            Map<String, Object> info = new java.util.HashMap<>();
            info.put("plan", planType);
            info.put("id", sub.getId() != null ? sub.getId() : "");
            info.put("status", sub.getStatus() != null ? sub.getStatus() : "");
            info.put("current_period_end", currentPeriodEnd);
            info.put("cancel_at_period_end", sub.getCancelAtPeriodEnd());
            info.put("canceled_at", sub.getCanceledAt());
            info.put("price_id", priceId != null ? priceId : "");
            logger.info("[getSubscriptionInfo] Returning info: {}", info);
            return ResponseEntity.ok(info);
        } catch (IllegalArgumentException e) {
            logger.error("[getSubscriptionInfo] Not found: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("[getSubscriptionInfo] Unexpected error: {}", e.getMessage(), e);
            String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred: " + msg));
        } finally {
            logger.info("[getSubscriptionInfo] Exit");
        }
    }
    // DTO for request body
    public static class CreateCheckoutSessionRequest {
        private SubscriptionPlan plan;
        private SubscriptionDuration duration;
        private String successUrl;
        private String cancelUrl;

        public SubscriptionPlan getPlan() { return plan; }
        public void setPlan(SubscriptionPlan plan) { this.plan = plan; }
        public SubscriptionDuration getDuration() { return duration; }
        public void setDuration(SubscriptionDuration duration) { this.duration = duration; }
        public String getSuccessUrl() { return successUrl; }
        public void setSuccessUrl(String successUrl) { this.successUrl = successUrl; }
        public String getCancelUrl() { return cancelUrl; }
        public void setCancelUrl(String cancelUrl) { this.cancelUrl = cancelUrl; }
    }
} 