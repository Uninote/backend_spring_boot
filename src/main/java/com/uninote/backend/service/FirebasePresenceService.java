package com.uninote.backend.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.UserRepository;

@Service
public class FirebasePresenceService {

    private static final Logger logger = LoggerFactory.getLogger(FirebasePresenceService.class);

    @Autowired
    private FirebaseDatabase firebaseDatabase;

    @Autowired
    private UserRepository userRepository;
    
    public boolean isUserActive(Long userId) {
        // Only log for user 330
        boolean isTargetUser = userId == 330L;
        
        if (isTargetUser) {
            logger.debug("=== FIREBASE PRESENCE CHECK FOR USER 330 ===");
            logger.debug("Checking presence for user ID: {}", userId);
        }
        
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                if (isTargetUser) {
                    logger.error("User 330 not found in database");
                }
                return false;
            }
            
            if (isTargetUser) {
                logger.debug("Found user 330 in database - Firebase UID: {}", user.getFirebaseUid());
            }
            
            DatabaseReference statusRef = firebaseDatabase.getReference("status/" + user.getFirebaseUid());
            DatabaseReference stateRef = statusRef.child("state");
            
            if (isTargetUser) {
                logger.debug("Checking Firebase path: status/{}/state", user.getFirebaseUid());
            }
            
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            
            stateRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (isTargetUser) {
                        logger.debug("Firebase data received for user 330");
                        logger.debug("Data exists: {}", dataSnapshot.exists());
                        if (dataSnapshot.exists()) {
                            String state = dataSnapshot.getValue(String.class);
                            logger.debug("Firebase state value: '{}'", state);
                            boolean isOnline = "online".equals(state);
                            logger.debug("User 330 is online: {}", isOnline);
                            future.complete(isOnline);
                        } else {
                            logger.debug("No Firebase data found for user 330 - treating as offline");
                            future.complete(false);
                        }
                    } else {
                        String state = dataSnapshot.getValue(String.class);
                        boolean isOnline = "online".equals(state);
                        future.complete(isOnline);
                    }
                }
                
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    if (isTargetUser) {
                        logger.error("Firebase database error for user 330: {}", databaseError.getMessage());
                        logger.error("Error code: {}", databaseError.getCode());
                        logger.error("Error details: {}", databaseError.getDetails());
                    }
                    future.complete(false); 
                }
            });
            
            boolean result = future.get(10, TimeUnit.SECONDS);
            if (isTargetUser) {
                logger.debug("=== FIREBASE PRESENCE RESULT FOR USER 330: {} ===", result);
            }
            return result;
            
        } catch (java.util.concurrent.TimeoutException e) {
            if (isTargetUser) {
                logger.error("=== FIREBASE PRESENCE TIMEOUT FOR USER 330 ===");
                logger.error("Firebase connection timed out after 10 seconds");
                logger.error("This usually means Firebase Realtime Database is not accessible");
                logger.error("Check Firebase configuration and network connectivity");
            }
            return false;
        } catch (Exception e) {
            if (isTargetUser) {
                logger.error("=== FIREBASE PRESENCE ERROR FOR USER 330 ===");
                logger.error("Exception type: {}", e.getClass().getSimpleName());
                logger.error("Error message: {}", e.getMessage());
                logger.error("Stack trace:", e);
            } else {
                System.err.println("Error checking Firebase presence for user " + userId + ": " + e.getMessage());
            }
            return false; 
        }
    }

    
    public boolean isUserActive(Long userId, int timeoutMinutes) {
        try {
            DatabaseReference statusRef = firebaseDatabase.getReference("status/" + userId);
            
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            
            statusRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        future.complete(false);
                        return;
                    }
                    
                    String state = dataSnapshot.child("state").getValue(String.class);
                    Long lastChanged = dataSnapshot.child("last_changed").getValue(Long.class);
                    
                    if (!"online".equals(state) || lastChanged == null) {
                        future.complete(false);
                        return;
                    }
                    
                    // Check if last activity was within timeout
                    long currentTime = System.currentTimeMillis();
                    long minutesSinceLastActivity = (currentTime - lastChanged) / (1000 * 60);
                    
                    future.complete(minutesSinceLastActivity <= timeoutMinutes);
                }
                
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    future.complete(false);
                }
            });
            
            return future.get(5, TimeUnit.SECONDS);
            
        } catch (Exception e) {
            System.err.println("Error checking Firebase presence for user " + userId + ": " + e.getMessage());
            return false;
        }
    }

    public boolean wasUserActiveRecently(Long userId, int minutes) {
        return isUserActive(userId, minutes);
    }

    
    public Long getLastActivityTimestamp(Long userId) {
        try {
            DatabaseReference statusRef = firebaseDatabase.getReference("status/" + userId);
            DatabaseReference lastChangedRef = statusRef.child("last_changed");
            
            CompletableFuture<Long> future = new CompletableFuture<>();
            
            lastChangedRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Long timestamp = dataSnapshot.getValue(Long.class);
                    future.complete(timestamp);
                }
                
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    future.complete(null);
                }
            });
            
            return future.get(5, TimeUnit.SECONDS);
            
        } catch (Exception e) {
            System.err.println("Error getting last activity for user " + userId + ": " + e.getMessage());
            return null;
        }
    }

    public boolean[] areUsersActive(List<Long> userIds) {
        boolean[] results = new boolean[userIds.size()];
        
        for (int i = 0; i < userIds.size(); i++) {
            results[i] = isUserActive(userIds.get(i));
        }
        
        return results;
    }
    
    /**
     * Simple fallback method for testing - treats user 330 as always online
     * Use this when Firebase is not configured or accessible
     */
    public boolean isUserActiveForTesting(Long userId) {
        if (userId == 330L) {
            logger.info("=== TESTING MODE: Treating user 330 as ONLINE ===");
            return true; // Always treat user 330 as online for testing
        }
        return false;
    }
} 