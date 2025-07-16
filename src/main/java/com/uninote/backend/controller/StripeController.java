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
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.UserRepository;
import com.uninote.backend.service.StripeService;
import com.uninote.backend.config.security.FirebaseAuthentication;


@RestController
@RequestMapping("/api/stripe")
public class StripeController {

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