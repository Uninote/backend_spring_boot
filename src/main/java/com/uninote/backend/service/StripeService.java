package com.uninote.backend.service;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Invoice;
import com.stripe.model.Price;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.uninote.backend.entity.SubscriptionDuration;
import com.uninote.backend.entity.SubscriptionPlan;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.UserRepository;

@Service
public class StripeService {

    private static final Logger logger = LoggerFactory.getLogger(StripeService.class);

    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final Environment env;

    private final Map<SubscriptionPlan, Map<SubscriptionDuration, String>> priceIdMap = new EnumMap<>(SubscriptionPlan.class);

    public StripeService(UserRepository userRepository,
                         SubscriptionService subscriptionService,
                         Environment env) {
        this.userRepository = userRepository;
        this.subscriptionService = subscriptionService;
        this.env = env;
    }

    @PostConstruct
    private void initPriceMap() {
        for (SubscriptionPlan plan : SubscriptionPlan.values()) {
            Map<SubscriptionDuration, String> durationMap = new EnumMap<>(SubscriptionDuration.class);
            for (SubscriptionDuration duration : SubscriptionDuration.values()) {
                String key = "stripe.price." + plan.name() + "_" + duration.name();
                String value = env.getProperty(key);
                if (value != null) {
                    durationMap.put(duration, value);
                }
            }
            priceIdMap.put(plan, durationMap);
        }
    }

    public Customer createOrRetrieveCustomer(User user) throws StripeException {
        if (user.getStripeCustomerId() != null) {
            return Customer.retrieve(user.getStripeCustomerId());
        }

        CustomerCreateParams params = CustomerCreateParams.builder()
                .setEmail(user.getEmail())
                .build();

        Customer customer = Customer.create(params);
        user.setStripeCustomerId(customer.getId());
        userRepository.save(user);

        return customer;
    }

    public String getPriceId(SubscriptionPlan plan, SubscriptionDuration duration) {
        String priceId = priceIdMap
            .getOrDefault(plan, Map.of())
            .get(duration);

        if (priceId == null) {
            throw new IllegalArgumentException("Missing price ID for plan=" + plan + ", duration=" + duration);
        }

        return priceId;
    }

    public void handleCheckoutSessionCompleted(Session session) throws StripeException {
        String customerId = session.getCustomer();
        String subscriptionId = session.getSubscription();

        Customer customer = Customer.retrieve(customerId);
        String email = customer.getEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Subscription stripeSub = Subscription.retrieve(subscriptionId);
        Price price = stripeSub.getItems().getData().get(0).getPrice();
        String priceId = price.getId();

        SubscriptionPlan plan = null;
        SubscriptionDuration duration = null;

        outer:
        for (Map.Entry<SubscriptionPlan, Map<SubscriptionDuration, String>> entry : priceIdMap.entrySet()) {
            for (Map.Entry<SubscriptionDuration, String> inner : entry.getValue().entrySet()) {
                if (inner.getValue().equals(priceId)) {
                    plan = entry.getKey();
                    duration = inner.getKey();
                    break outer;
                }
            }
        }

        if (plan == null || duration == null) {
            throw new IllegalArgumentException("Price ID not mapped in config: " + priceId);
        }

        subscriptionService.createSubscription(
                email,
                plan,
                duration,
                subscriptionId,
                customerId
        );
    }

    public void handleSubscriptionCancelled(Subscription stripeSubscription) throws StripeException {
        String stripeSubId = stripeSubscription.getId();
        // Optional: subscriptionRepository.markCancelled(stripeSubId);
    }

    /**
     * Cancels the user's active Stripe subscription so it does not renew.
     * @param userId The user's ID
     */
    public void cancelStripeSubscriptionRenewal(Long userId) throws StripeException {
        // Find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        // Find the user's active subscription
        com.uninote.backend.entity.Subscription activeSub = subscriptionService
                .findLatestActiveSubscriptionByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("No active subscription found for user."));
        String stripeSubId = activeSub.getStripeSubscriptionId();
        if (stripeSubId == null || stripeSubId.isEmpty()) {
            throw new IllegalStateException("No Stripe subscription ID found for user.");
        }
        // Cancel the subscription in Stripe (set cancel_at_period_end = true)
        Subscription stripeSub = Subscription.retrieve(stripeSubId);
        Subscription updatedSub = stripeSub.cancel(Map.of("invoice_now", false, "prorate", false));
        // Optionally, you can also set cancel_at_period_end = true instead of immediate cancel:
        // SubscriptionUpdateParams params = SubscriptionUpdateParams.builder().setCancelAtPeriodEnd(true).build();
        // Subscription updatedSub = stripeSub.update(params);
        // Update local DB
        subscriptionService.saveSubscription(activeSub);
    }

    /**
     * Retrieves the user's latest active Stripe subscription info from Stripe.
     * @param userId The user's ID
     * @return The Stripe Subscription object
     */
    public Subscription getStripeSubscriptionInfo(Long userId) throws StripeException {
        logger.info("[getStripeSubscriptionInfo] Start for userId={}", userId);
        Optional<com.uninote.backend.entity.Subscription> opt = subscriptionService.findLatestActiveSubscriptionByUserId(userId);
        if (opt.isEmpty()) {
            logger.warn("[getStripeSubscriptionInfo] No active subscription found for user {}", userId);
            throw new IllegalStateException("No active subscription found for user.");
        }
        com.uninote.backend.entity.Subscription latestActiveSub = opt.get();
        String stripeSubId = latestActiveSub.getStripeSubscriptionId();
        if (stripeSubId == null || stripeSubId.isEmpty()) {
            logger.warn("[getStripeSubscriptionInfo] No Stripe subscription ID found for user {}", userId);
            throw new IllegalStateException("No Stripe subscription ID found for user.");
        }
        try {
            logger.debug("[getStripeSubscriptionInfo] Retrieving Stripe subscription from Stripe API: {}", stripeSubId);
            Subscription stripeSub = Subscription.retrieve(stripeSubId);
            if (stripeSub == null) {
                logger.error("[getStripeSubscriptionInfo] Stripe API returned null for subscription ID {} (user {})", stripeSubId, userId);
                throw new IllegalStateException("Stripe subscription not found.");
            }
            logger.info("[getStripeSubscriptionInfo] Successfully retrieved Stripe subscription for user {}: {}", userId, stripeSubId);
            return stripeSub;
        } catch (Exception e) {
            logger.error("[getStripeSubscriptionInfo] Error retrieving Stripe subscription from Stripe for user {}: {}", userId, e.getMessage(), e);
            throw e;
        } finally {
            logger.info("[getStripeSubscriptionInfo] End for userId={}", userId);
        }
    }

    /**
     * Creates a Stripe Checkout Session for a user for a given plan and duration.
     * @param userId The user's ID
     * @param plan The subscription plan
     * @param duration The subscription duration
     * @param successUrl The URL to redirect to after successful payment
     * @param cancelUrl The URL to redirect to if payment is cancelled
     * @return The Stripe Checkout Session URL
     * @throws StripeException if Stripe API fails
     */
    public String createCheckoutSession(Long userId, SubscriptionPlan plan, SubscriptionDuration duration, String successUrl, String cancelUrl) throws StripeException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        Customer customer = createOrRetrieveCustomer(user);
        String priceId = getPriceId(plan, duration);

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setCustomer(customer.getId())
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(priceId)
                                .setQuantity(1L)
                                .build()
                )
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .setClientReferenceId(userId.toString())
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }

    // Add this method to allow controller to get the latest active subscription entity
    public java.util.Optional<com.uninote.backend.entity.Subscription>  getLatestActiveSubscriptionEntity(Long userId) {
        return subscriptionService.findLatestActiveSubscriptionByUserId(userId);
    }

    /**
     * Handles a Stripe invoice.paid event for subscription renewals.
     * @param event The Stripe event object
     */
    public void handleInvoicePaid(com.stripe.model.Event event) {
        logger.info("[handleInvoicePaid] Start processing invoice.paid event");
        try {
            // Get the invoice object from the event
            Invoice invoice = null;
            if (event.getDataObjectDeserializer().getObject().isPresent()) {
                Object obj = event.getDataObjectDeserializer().getObject().get();
                if (obj instanceof Invoice) {
                    invoice = (Invoice) obj;
                }
            }
            if (invoice == null) {
                logger.error("[handleInvoicePaid] Invoice object is null in event data");
                return;
            }
            String stripeSubId = null;
            try {
                stripeSubId = (String) Invoice.class.getMethod("getSubscription").invoke(invoice);
            } catch (Exception e) {
                logger.error("[handleInvoicePaid] Could not get subscription ID from invoice: {}", e.getMessage(), e);
                return;
            }
            if (stripeSubId == null) {
                logger.error("[handleInvoicePaid] No subscription ID in invoice");
                return;
            }
            // Retrieve the Stripe subscription
            com.stripe.model.Subscription stripeSub = com.stripe.model.Subscription.retrieve(stripeSubId);
            String customerId = stripeSub.getCustomer();
            com.stripe.model.Customer customer = com.stripe.model.Customer.retrieve(customerId);
            String email = customer.getEmail();
            if (email == null) {
                logger.error("[handleInvoicePaid] No email found for customer {}", customerId);
                return;
            }
            // Get priceId from the subscription's first item
            String priceId = stripeSub.getItems().getData().get(0).getPrice().getId();
            SubscriptionPlan plan = null;
            SubscriptionDuration duration = null;
            outer:
            for (Map.Entry<SubscriptionPlan, Map<SubscriptionDuration, String>> entry : priceIdMap.entrySet()) {
                for (Map.Entry<SubscriptionDuration, String> inner : entry.getValue().entrySet()) {
                    if (inner.getValue().equals(priceId)) {
                        plan = entry.getKey();
                        duration = inner.getKey();
                        break outer;
                    }
                }
            }
            if (plan == null || duration == null) {
                logger.error("[handleInvoicePaid] Price ID {} not mapped to plan/duration", priceId);
                return;
            }
            // Check if a subscription for this period already exists (avoid duplicate renewals)
            Optional<com.uninote.backend.entity.Subscription> latest = subscriptionService.findLatestActiveSubscriptionByUserId(
                userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found")).getId()
            );
            if (latest.isPresent()) {
                com.uninote.backend.entity.Subscription last = latest.get();
                if (last.getEndDate() != null) {
                    java.time.LocalDate today = java.time.LocalDate.now();
                    java.time.LocalDate endDate = last.getEndDate().toLocalDate();
                    // If the end date is after today, skip renewal. If it's today or before, allow renewal.
                    if (endDate.isAfter(today)) {
                        logger.info("[handleInvoicePaid] User already has an active subscription ending at {} (after today). Skipping renewal.", last.getEndDate());
                        return;
                    }
                }
            }
            // Create a new subscription period in the DB
            subscriptionService.createSubscription(
                email,
                plan,
                duration,
                stripeSubId,
                customerId
            );
            logger.info("[handleInvoicePaid] Successfully renewed subscription for {} (plan={}, duration={})", email, plan, duration);
        } catch (Exception e) {
            logger.error("[handleInvoicePaid] Error processing invoice.paid event: {}", e.getMessage(), e);
        }
    }
}
