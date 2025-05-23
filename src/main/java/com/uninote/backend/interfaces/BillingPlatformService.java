package com.uninote.backend.interfaces;

import com.uninote.backend.entity.Subscription;
import com.uninote.backend.entity.SubscriptionDuration;
import com.uninote.backend.entity.User;

public interface BillingPlatformService {
    String getPriceIdForDuration(SubscriptionDuration duration);
    User createOrRetrieveCustomer(User user);
    void handleCheckoutSessionCompleted(User user, SubscriptionDuration duration);
}
