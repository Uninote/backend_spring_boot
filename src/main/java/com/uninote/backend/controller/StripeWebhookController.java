package com.uninote.backend.controller;

import com.stripe.Stripe;
import com.stripe.model.Subscription;
import com.stripe.exception.StripeException;
import com.uninote.backend.config.security.FirebaseAuthentication;
import com.uninote.backend.dto.StripeWebhookPayload;
import com.uninote.backend.entity.SubscriptionDuration;
import com.uninote.backend.entity.SubscriptionPlan;
import com.uninote.backend.service.SubscriptionService;
import com.uninote.backend.repository.UserRepository;
import com.uninote.backend.entity.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;


@RestController
@RequestMapping("/api/stripe")
public class StripeWebhookController {

    private final SubscriptionService subscriptionService;

    @Autowired
    private UserRepository userRepository;

    public StripeWebhookController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/webhook-sync")
    public ResponseEntity<?> syncSubscription(@RequestBody StripeWebhookPayload payload) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        FirebaseAuthentication firebaseAuth = (FirebaseAuthentication) authentication;
        String userUid = firebaseAuth.getUid();

        User user = userRepository.findByFirebaseUid(userUid)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.getEmail().equalsIgnoreCase(payload.getCustomerEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Email mismatch between Firebase user and Stripe payload"));
        }
        try {
            subscriptionService.handleStripeWebhook(
                payload.getCustomerEmail(),
                payload.getSubscriptionId(),
                payload.getDuration()
            );
            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}


