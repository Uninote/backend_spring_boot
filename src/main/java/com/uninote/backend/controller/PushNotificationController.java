package com.uninote.backend.controller;

import com.uninote.backend.entity.PushSubscription;
import com.uninote.backend.service.PushNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/push")
public class PushNotificationController {

    @Autowired
    private PushNotificationService pushNotificationService;

    @PostMapping("/subscribe")
    public ResponseEntity<String> saveSubscription(@RequestBody PushSubscription subscription) {
        pushNotificationService.saveSubscription(subscription);
        return ResponseEntity.ok("Subscription saved successfully.");
    }

    @GetMapping("/subscription/{userId}")
    public ResponseEntity<PushSubscription> getSubscription(@PathVariable Long userId) {
        PushSubscription subscription = pushNotificationService.getSubscriptionByUserId(userId);
        if (subscription != null) {
            return ResponseEntity.ok(subscription);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/notify/{userId}")
    public ResponseEntity<String> sendNotification(@PathVariable Long userId) {
        PushSubscription subscription = pushNotificationService.getSubscriptionByUserId(userId);

        if (subscription == null) {
            return ResponseEntity.status(404).body("No subscription found for user ID " + userId);
        }

        pushNotificationService.sendPushNotification(
            subscription,
            "Test Notification",
            "This is a test message",
            "https://your-app.com/test"
        );

        return ResponseEntity.ok("Notification sent.");
    }
}
