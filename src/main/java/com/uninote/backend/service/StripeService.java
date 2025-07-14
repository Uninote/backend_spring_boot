package com.uninote.backend.service;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Price;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.uninote.backend.entity.SubscriptionDuration;
import com.uninote.backend.entity.SubscriptionPlan;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.UserRepository;
import com.uninote.backend.service.SubscriptionService;

@Service
public class StripeService {

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
                .setCustomerEmail(user.getEmail())
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }
}
