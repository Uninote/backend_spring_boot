package com.uninote.backend.service;

import com.uninote.backend.entity.SubscriptionDuration;
import com.uninote.backend.entity.SubscriptionPlan;
import com.uninote.backend.entity.User;
import com.uninote.backend.interfaces.BillingPlatformService;
import com.uninote.backend.repository.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Profile("dev")
public class LocalStripeService implements BillingPlatformService {

    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;

    public LocalStripeService(UserRepository userRepository, SubscriptionService subscriptionService) {
        this.userRepository = userRepository;
        this.subscriptionService = subscriptionService;
    }

    @Override
    public String getPriceIdForDuration(SubscriptionDuration duration) {
        return "local-price-" + duration.name().toLowerCase();
    }

    @Override
    public User createOrRetrieveCustomer(User user) {
        if (user.getStripeCustomerId() == null) {
            String mockCustomerId = "cus_" + UUID.randomUUID();
            user.setStripeCustomerId(mockCustomerId);
            userRepository.save(user);
        }
        return user;
    }

    @Override
    public void handleCheckoutSessionCompleted(User user, SubscriptionDuration duration) {
        String mockSubscriptionId = "sub_" + UUID.randomUUID();

        subscriptionService.createSubscription(
            user.getEmail(),
            SubscriptionPlan.BASIC,
            duration,
            mockSubscriptionId,
            user.getStripeCustomerId()
        );
    }
}
