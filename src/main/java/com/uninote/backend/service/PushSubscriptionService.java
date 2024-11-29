package com.uninote.backend.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uninote.backend.entity.PushSubscription;
import com.uninote.backend.repository.PushSubscriptionRepository;

import java.util.Optional;

@Service
public class PushSubscriptionService {

    @Autowired
    private PushSubscriptionRepository pushSubscriptionRepository;

    /**
     *
     * @param subscription the subscription to save.
     */
    public void saveSubscription(PushSubscription subscription) {
        Optional<PushSubscription> existingSubscription = Optional.ofNullable(
            pushSubscriptionRepository.findByUserId(subscription.getUserId())
        );

        if (existingSubscription.isPresent()) {
            PushSubscription existing = existingSubscription.get();
            existing.setEndpoint(subscription.getEndpoint());
            existing.setP256dh(subscription.getP256dh());
            existing.setAuth(subscription.getAuth());
            pushSubscriptionRepository.save(existing);
        } else {
            pushSubscriptionRepository.save(subscription);
        }
    }

    /**
     * Retrieve a subscription by user ID.
     *
     * @param userId the user ID.
     * @return the push subscription, or null if not found.
     */
    public PushSubscription getSubscriptionByUserId(Long userId) {
        return pushSubscriptionRepository.findByUserId(userId);
    }

    /**
     * Delete a subscription by user ID.
     *
     * @param userId the user ID.
     */
    public void deleteSubscriptionByUserId(Long userId) {
        PushSubscription subscription = pushSubscriptionRepository.findByUserId(userId);
        if (subscription != null) {
            pushSubscriptionRepository.delete(subscription);
        }
    }
}
