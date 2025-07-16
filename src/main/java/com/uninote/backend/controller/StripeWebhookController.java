package com.uninote.backend.controller;

import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.uninote.backend.repository.UserRepository;
import com.uninote.backend.service.StripeService;
import com.uninote.backend.service.SubscriptionService;


@RestController
@RequestMapping("/api/stripe")
public class StripeWebhookController {

    private final SubscriptionService subscriptionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StripeService stripeService;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    private static final Logger logger = LoggerFactory.getLogger(StripeWebhookController.class);

    public StripeWebhookController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/webhook-sync")
    public ResponseEntity<String> handleStripeWebhook(HttpServletRequest request) {
        String payload = "";
        String sigHeader = request.getHeader("Stripe-Signature");

        try (Scanner s = new Scanner(request.getInputStream(), "UTF-8")) {
            payload = s.useDelimiter("\\A").hasNext() ? s.next() : "";
        } catch (Exception e) {
            logger.error("[Stripe Webhook] Error reading payload: {}", e.getMessage(), e);
            return ResponseEntity.status(400).body("Invalid payload");
        }

        logger.info("[Stripe Webhook] Received payload: {}", payload);
        logger.info("[Stripe Webhook] Stripe-Signature: {}", sigHeader);

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            logger.error("[Stripe Webhook] Invalid signature: {}", e.getMessage(), e);
            return ResponseEntity.status(400).body("Invalid signature");
        }

        logger.info("[Stripe Webhook] Event type: {}", event.getType());

        // Handle the event
        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if (session != null) {
                try {
                    logger.info("[Stripe Webhook] Handling checkout.session.completed for session: {}", session.getId());
                    stripeService.handleCheckoutSessionCompleted(session);
                } catch (Exception e) {
                    logger.error("[Stripe Webhook] Error handling session: {}", e.getMessage(), e);
                    return ResponseEntity.status(500).body("Error handling session: " + e.getMessage());
                }
            } else {
                logger.warn("[Stripe Webhook] Session object is null in event data.");
            }
        } else if ("invoice.paid".equals(event.getType())) {
            logger.info("[Stripe Webhook] Handling invoice.paid event");
            // TODO: Implement renewal logic in stripeService.handleInvoicePaid(event)
            // stripeService.handleInvoicePaid(event);
        } else if ("customer.subscription.updated".equals(event.getType())) {
            logger.info("[Stripe Webhook] Handling customer.subscription.updated event");
            // TODO: Implement subscription update logic in stripeService.handleSubscriptionUpdated(event)
            // stripeService.handleSubscriptionUpdated(event);
        }
        // ... handle other event types if needed

        return ResponseEntity.ok("");
    }
}


