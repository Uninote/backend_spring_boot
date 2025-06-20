package com.uninote.backend.service;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninote.backend.entity.PushSubscription;
import com.uninote.backend.repository.PushSubscriptionRepository;

import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;

@Service
public class PushNotificationService {

    @Autowired
    private PushSubscriptionRepository pushSubscriptionRepository;

    private final PushService pushService;

    public PushNotificationService() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        // Register BouncyCastle provider if not already registered
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }
        
        this.pushService = new PushService();
        pushService.setPublicKey("BKx2CN9aZTw3v7v_8KkZSKifgQ6SD5TKtAbEaYTXHMRIXuPk22FEGSyrwuJVD2X5sEWRekfUK0uggDazd0Iigt4");
        pushService.setPrivateKey("uLWs8QfAWI8n5HeC8He80vRpvvl76o2Nv7_QRiVL9GU");
    }

    /**
     * Save or update a push subscription.
     *
     * @param subscription the subscription to save or update
     */
    public void saveSubscription(PushSubscription subscription) {
        PushSubscription existing = pushSubscriptionRepository.findByUserId(subscription.getUserId());
        if (existing != null) {
            existing.setEndpoint(subscription.getEndpoint());
            existing.setP256dh(subscription.getP256dh());
            existing.setAuth(subscription.getAuth());
            pushSubscriptionRepository.save(existing);
        } else {
            pushSubscriptionRepository.save(subscription);
        }
    }

    /**
     * Retrieve a push subscription by user ID.
     *
     * @param userId the user ID
     * @return the push subscription or null if not found
     */
    public PushSubscription getSubscriptionByUserId(Long userId) {
        return pushSubscriptionRepository.findByUserId(userId);
    }

    /**
     * Send a push notification.
     *
     * @param subscription the subscription to send the notification to
     * @param title the title of the notification
     * @param body the body of the notification
     * @param url the URL to open when the notification is clicked
     */
    public void sendPushNotification(PushSubscription subscription, String title, String body, String url) {
        try {
            String payload = new ObjectMapper().writeValueAsString(Map.of(
                "title", title,
                "body", body,
                "url", url
            ));

            Notification notification = new Notification(
                subscription.getEndpoint(),
                subscription.getP256dh(),
                subscription.getAuth(),
                payload
            );

            pushService.send(notification);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
