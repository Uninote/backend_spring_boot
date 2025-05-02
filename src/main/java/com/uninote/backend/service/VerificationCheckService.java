package com.uninote.backend.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

import javax.annotation.PostConstruct;

import com.uninote.backend.entity.User;
import com.uninote.backend.repository.UserRepository;

@Service
public class VerificationCheckService {
    
    private static final Logger logger = LoggerFactory.getLogger(VerificationCheckService.class);
    
    private final FirebaseAuth firebaseAuth;
    private final UserRepository userRepository; 

    @Autowired
    private UserService userService;

    @Autowired
    public VerificationCheckService(FirebaseAuth firebaseAuth, UserRepository userRepository) {
        this.firebaseAuth = firebaseAuth;
        this.userRepository = userRepository;
    }
   
  
    @Scheduled(fixedRate = 3600000) 
    public void checkEmailVerifications() {
        logger.info("Starting scheduled email verification check");
        
        List<User> unverifiedUsers = userRepository.findByEmailVerifiedFalse();
        
        int verifiedCount = 0;
        for (User user : unverifiedUsers) {
            if (user.getFirebaseUid() == null || user.getFirebaseUid().isEmpty()) {
                logger.warn("Skipping user with ID {} due to null or empty Firebase UID", user.getId());
                continue;
            }
            try {
                UserRecord userRecord = firebaseAuth.getUser(user.getFirebaseUid());
                
                if (userRecord.isEmailVerified()) {
                    user.setEmailVerified(true);
                    userRepository.save(user);
                    verifiedCount++;
                    
                    handleNewlyVerifiedUser(user);
                }
            } catch (FirebaseAuthException e) {
                logger.error("Error checking verification status for user {}: {}", 
                             user.getFirebaseUid(), e.getMessage());
            }
        }
        
        logger.info("Completed verification check. Found {} newly verified users", verifiedCount);
    }
    
   
    private void handleNewlyVerifiedUser(User user) {
        userService.verfiyEmail(user.getFirebaseUid());
        logger.info("User {} has verified their email", user.getEmail());
    }
}