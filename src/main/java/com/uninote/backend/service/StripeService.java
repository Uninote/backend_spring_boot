package com.uninote.backend.service;


import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Price;
import com.stripe.model.checkout.Session;
import com.stripe.model.Subscription;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.uninote.backend.entity.SubscriptionDuration;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class StripeService {

    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;

    @Value("${stripe.price.one_day}")
    private String oneDayPriceId;

    @Value("${stripe.price.one_month}")
    private String oneMonthPriceId;

    @Value("${stripe.price.one_year}")
    private String oneYearPriceId;

    public StripeService(UserRepository userRepository, SubscriptionService subscriptionService) {
        this.userRepository = userRepository;
        this.subscriptionService = subscriptionService;
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

    public String getPriceIdForDuration(SubscriptionDuration duration) {
    if (duration == SubscriptionDuration.ONE_DAY) {
        return oneDayPriceId;
    } else if (duration == SubscriptionDuration.ONE_MONTH) {
        return oneMonthPriceId;
    } else if (duration == SubscriptionDuration.ONE_YEAR) {
        return oneYearPriceId;
    } else {
        throw new IllegalArgumentException("Unknown subscription duration: " + duration);
    }
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

        SubscriptionDuration duration = mapPriceIdToDuration(priceId);

        /*subscriptionService.createSubscription(
                email,
                duration,
                subscriptionId,
                customerId
        );*/
    }

    public void handleSubscriptionCancelled(Subscription stripeSubscription) throws StripeException {
        String stripeSubId = stripeSubscription.getId();
        // Optional: remove from DB or mark as cancelled
        // Example: subscriptionRepository.updateStatusByStripeSubscriptionId(stripeSubId, "cancelled");
    }

    private SubscriptionDuration mapPriceIdToDuration(String priceId) {
        if (priceId.equals(oneDayPriceId)) {
            return SubscriptionDuration.ONE_DAY;
        } else if (priceId.equals(oneMonthPriceId)) {
            return SubscriptionDuration.ONE_MONTH;
        } else if (priceId.equals(oneYearPriceId)) {
            return SubscriptionDuration.ONE_YEAR;
        } else {
            throw new IllegalArgumentException("Unknown price ID: " + priceId);
        }
    }
}
