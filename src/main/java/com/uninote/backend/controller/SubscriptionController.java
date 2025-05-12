package com.uninote.backend.controller;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.uninote.backend.entity.Subscription;
import com.uninote.backend.entity.SubscriptionDuration;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.UserRepository;
import com.uninote.backend.service.StripeService;
import com.uninote.backend.service.SubscriptionService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.model.checkout.Session;


@RestController
@RequestMapping("/subscription")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StripeService stripeService;

    

    @PostMapping("/create-checkout-session")
    public ResponseEntity<?> createCheckoutSession(
            @AuthenticationPrincipal(expression = "email") String email,
            @RequestParam SubscriptionDuration duration) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            Customer stripeCustomer = stripeService.createOrRetrieveCustomer(user);
            String priceId = stripeService.getPriceIdForDuration(duration);

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setCustomer(stripeCustomer.getId())
                    .setSuccessUrl("https://your-app.com/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl("https://your-app.com/cancel")
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPrice(priceId)
                            .build())
                    .build();

            Session session = Session.create(params);

            return ResponseEntity.ok(Map.of("sessionId", session.getId()));
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Stripe error: " + e.getMessage());
        }
    }


}
