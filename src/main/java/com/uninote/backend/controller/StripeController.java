package com.uninote.backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uninote.backend.entity.SubscriptionDuration;
import com.uninote.backend.entity.SubscriptionPlan;
import com.uninote.backend.service.StripeService;

@RestController
@RequestMapping("/api/stripe")
public class StripeController {

    private final StripeService stripeService;

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
        try {
            String url;
            try {
                url = stripeService.createCheckoutSession(
                        request.getUserId(),
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

    // DTO for request body
    public static class CreateCheckoutSessionRequest {
        private Long userId;
        private SubscriptionPlan plan;
        private SubscriptionDuration duration;
        private String successUrl;
        private String cancelUrl;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
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