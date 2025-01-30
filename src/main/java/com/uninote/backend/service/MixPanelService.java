package com.uninote.backend.service;

import com.mixpanel.mixpanelapi.ClientDelivery;
import com.mixpanel.mixpanelapi.MessageBuilder;
import com.mixpanel.mixpanelapi.MixpanelAPI;
import com.uninote.backend.interfaceProjection.UserProfileProjection;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;


import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

@Service
public class MixPanelService {
    private static final Logger logger = LoggerFactory.getLogger(MixPanelService.class);
    private final MessageBuilder messageBuilder;
    private final MixpanelAPI mixpanel;
    private final Set<String> allowedEvents = new HashSet<>();
    private final UserService userService;

    @Autowired
    public MixPanelService(
            @Value("3731c549b800c46f7601e9db0c9f2e2e") String projectToken,
            @Lazy UserService userService) {
            this.messageBuilder = new MessageBuilder("3731c549b800c46f7601e9db0c9f2e2e");
            this.mixpanel = new MixpanelAPI();
            this.userService = userService;
            initializeAllowedEvents();
        }

        private void initializeAllowedEvents() {
            allowedEvents.add("Download");
            allowedEvents.add("Note View");
            allowedEvents.add("Upload Click");
            allowedEvents.add("Upload Finish");
            allowedEvents.add("Sign Up Click");
            allowedEvents.add("Sign Up Finish");
            allowedEvents.add("Internal PDF Chat");
            allowedEvents.add("Tutie File Chat");
            allowedEvents.add("Tutie Search Prompt");
            allowedEvents.add("Tutie Summary Quiz Generation");
            allowedEvents.add("Tutie Chat Response");
            allowedEvents.add("SignUp Navigation");
            allowedEvents.add("App Visit");
        }

    public boolean isAllowedEvent(String eventName) {
        return allowedEvents.contains(eventName);
    }

    public void trackEvent(long userId, String eventName, JSONObject properties) {
        if (!isAllowedEvent(eventName)) {
            logger.warn("Event '{}' is not allowed and will not be tracked.", eventName);
            return;
        }

        try {
            String userIdString = String.valueOf(userId);
            properties.put("distinct_id", userIdString);

            JSONObject eventMessage = messageBuilder.event(userIdString, eventName, properties);
            mixpanel.sendMessage(eventMessage);
            logger.info("Tracked event: {} for user_id: {}", eventName, userIdString);
        } catch (Exception e) {
            logger.error("Failed to track event: {}", eventName, e);
        }
    }

    @PostConstruct
    private void validateConfiguration() {
        logger.info("Validating Mixpanel configuration...");
        if (messageBuilder == null) {
            logger.error("MessageBuilder not initialized - check project token");
        } else {
            logger.info("Mixpanel configuration validated successfully");
        }
    }

    public void identifyUser(String anonymousId, long userId) {
        try {
            String userIdString = String.valueOf(userId);
    
            // ✅ Fetch the user profile
            UserProfileProjection userProfile = userService.getUserProfileById(userId, "GR");
            if (userProfile == null) {
                logger.warn("No user profile found for userId: {}", userId);
                return;
            }
    
            // ✅ Step 1: Create and send alias event (formatted correctly)
            JSONObject aliasEvent = new JSONObject();
            aliasEvent.put("event", "$create_alias");
    
            JSONObject aliasProperties = new JSONObject();
            aliasProperties.put("distinct_id", anonymousId);  // Old ID
            aliasProperties.put("alias", userIdString);       // New user ID
    
            aliasEvent.put("properties", aliasProperties);
            mixpanel.sendMessage(aliasEvent);  // Send alias event
            logger.info("Created alias: {} → {}", anonymousId, userIdString);
    
            // ✅ Step 2: Create and send profile update
            JSONObject userProfileJson = new JSONObject();
            userProfileJson.put("$name", sanitizeValue(userProfile.getUsername()));
            userProfileJson.put("$email", sanitizeValue(userProfile.getEmail()));
            userProfileJson.put("university", sanitizeValue(userProfile.getUniversityName()));
            userProfileJson.put("department", sanitizeValue(userProfile.getDepartmentName()));
            userProfileJson.put("$ip", "0");  // Prevent geolocation update
    
            JSONObject profileUpdateMessage = new JSONObject();
            profileUpdateMessage.put("$distinct_id", userIdString);
            profileUpdateMessage.put("$set", userProfileJson);
    
            mixpanel.sendMessage(profileUpdateMessage);  // Send profile update
            logger.info("Successfully identified user {} and updated profile.", userIdString);
    
        } catch (Exception e) {
            logger.error("Failed to identify and set profile for user_id: {}", userId, e);
        }
    }
    
    



    private String sanitizeValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "Unknown";
        }
        return value.trim()
            .replace("\n", " ")
            .replace("\r", " ")
            .replace("\t", " ")
            .replaceAll("\\s+", " ");
    }

}
