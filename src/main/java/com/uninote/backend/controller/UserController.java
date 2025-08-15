package com.uninote.backend.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninote.backend.config.security.FirebaseAuthentication;
import com.uninote.backend.converter.EntityToDTOConverter;
import com.uninote.backend.dto.FreeTrialStatusDTO;
import com.uninote.backend.dto.MetadataRequest;
import com.uninote.backend.dto.UserDTO;
import com.uninote.backend.dto.UserLimitsDTO;
import com.uninote.backend.dto.UserStatsDTO;
import com.uninote.backend.entity.Subscription;
import com.uninote.backend.entity.SubscriptionDuration;
import com.uninote.backend.entity.SubscriptionPlan;
import com.uninote.backend.entity.User;
import com.uninote.backend.interfaceProjection.UserInfoProjection;
import com.uninote.backend.interfaceProjection.UserProfileProjection;
import com.uninote.backend.repository.DepartmentRepository;
import com.uninote.backend.repository.MessageRepository;
import com.uninote.backend.repository.UniversityRepository;
import com.uninote.backend.repository.UserRepository;
import com.uninote.backend.service.SubscriptionService;
import com.uninote.backend.service.UsageLimitService;
import com.uninote.backend.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UsageLimitService usageLimitService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long userId) {
        User user = userService.findById(userId);
        if (user != null) {
            UserDTO userDTO = EntityToDTOConverter.convertUserToDTO(user);
            return ResponseEntity.ok(userDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDto) {
        try {
            User newUser = userService.createUser(userDto);
            UserDTO newUserDTO = EntityToDTOConverter.convertUserToDTO(newUser);
            return new ResponseEntity<>(newUserDTO, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/leaderboard")
    public ResponseEntity<List<UserInfoProjection>> getLeaderboard(){
        List <UserInfoProjection> leaderborad = userService.getTop100UsersByUniscore();
        return ResponseEntity.ok(leaderborad);
    }

    @PutMapping("/{userId}/update")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long userId, @RequestBody UserDTO userDto) {
        try {
            User updatedUser = userService.updateUser(userId, userDto);
            if (updatedUser != null) {
                UserDTO updatedUserDTO = EntityToDTOConverter.convertUserToDTO(updatedUser);
                return ResponseEntity.ok(updatedUserDTO);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/leaderboard/university/{universityId}")
    public ResponseEntity<List<UserInfoProjection>> getLeaderboardByUniversity(@PathVariable Long universityId) {
        List<UserInfoProjection> leaderboard = userService.getTop100UsersByUniscoreByUniversity(universityId);
        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/leaderboard/department/{departmentId}")
    public ResponseEntity<List<UserInfoProjection>> getLeaderboardByDepartment(@PathVariable Long departmentId) {
        List<UserInfoProjection> leaderboard = userService.getTop100UsersByUniscoreByDepartment(departmentId);
        return ResponseEntity.ok(leaderboard);
    }

    @PutMapping("/{id}/login")
    public ResponseEntity<Long> loginUserAndUpdateStreak(@PathVariable Long id, @RequestParam(required = false) Boolean deviceId, @RequestParam(required = false) String anonymusSessionId) {

        Long sessionId;

        if(deviceId != null){
            sessionId = userService.loginUserAndUpdateStreak(id,deviceId);

        } else {

            sessionId = userService.loginUserAndUpdateStreak(id, anonymusSessionId);

        }
        return ResponseEntity.ok(sessionId);
    }

    @PostMapping("/{userId}/logout")
    public ResponseEntity<Void> logoutUserAndEndSession(@PathVariable Long userId) {
        userService.logoutUserAndEndSession(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/firebase/{firebaseUid}")
    public ResponseEntity<Long> findByFirebaseUid(@PathVariable String firebaseUid) {
        Long userId = userService.findByFirebaseUid(firebaseUid);
        return userId!= null ? ResponseEntity.ok(userId) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        try {
            userService.softDeleteUserById(userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{userId}/approve/{approvedUserId}")
    public ResponseEntity<User> approveUser(@PathVariable Long userId, @PathVariable Long approvedUserId) {
        User user = userService.approveUser(userId, approvedUserId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{userId}/approved_users")
    public ResponseEntity<Set<UserDTO>> getApprovedUsers(@PathVariable Long userId) {
        Set<User> approvedUsers = userService.getApprovedUsers(userId);
        Set<UserDTO> approvedUserDTOs = approvedUsers.stream().map(user -> {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setProfileImageUrl(user.getProfileImageUrl());
            userDTO.setDepartmentId(user.getDepartment() != null ? user.getDepartment().getId() : null);
            userDTO.setUniversityId(user.getUniversity() != null ? user.getUniversity().getId() : null);
            userDTO.setUsername(user.getUsername());
            return userDTO;
        }).collect(Collectors.toSet());
        return ResponseEntity.ok(approvedUserDTOs);
    }

    @GetMapping("/{userId}/approved_by_users")
    public ResponseEntity<Set<User>> getUsersWhoApproved(@PathVariable Long userId) {
        Set<User> approvedByUsers = userService.getUsersWhoApproved(userId);
        return ResponseEntity.ok(approvedByUsers);
    }

    @GetMapping("/{userId}/stats")
    public ResponseEntity<UserStatsDTO> getUserStats(@PathVariable Long userId) {
        try {
            UserStatsDTO stats = userService.getUserStats(userId);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }


    }

    @GetMapping("/check-username")
    public ResponseEntity<Boolean> validateUsername(@RequestParam String username) {
        boolean usernameExists = userService.doesUsernameExist(username);
        return ResponseEntity.ok(usernameExists);
    }

    @GetMapping("/check-email")
    public ResponseEntity<Boolean> validateEmail(@RequestParam String email) {
        boolean emailExists = userService.doesEmailExist(email);
        return ResponseEntity.ok(emailExists);
    }

     @GetMapping("/{userId}/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "EN") String language) {
        UserProfileProjection userProfile = userService.getUserProfileById(userId, language);
        if (userProfile == null) {
            return ResponseEntity.notFound().build();
        }
        // Fetch latest active subscription plan
        String plan = "FREE";
        try {
            java.util.Optional<com.uninote.backend.entity.Subscription> opt = subscriptionService.findLatestActiveSubscriptionByUserId(userId);
            if (opt.isPresent() && opt.get().getPlan() != null) {
                plan = opt.get().getPlan().name();
            }
        } catch (Exception e) {
            // Log and default to FREE
            org.slf4j.LoggerFactory.getLogger(UserController.class).warn("Could not fetch subscription plan for user {}: {}", userId, e.getMessage());
        }
        // Build response map with all userProfile fields and updated plan
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("id", userProfile.getId());
        response.put("firebaseUid", userProfile.getFirebaseUid());
        response.put("name", userProfile.getName());
        response.put("departmentName", userProfile.getDepartmentName());
        response.put("universityName", userProfile.getUniversityName());
        response.put("departmentFullName", userProfile.getDepartmentFullName());
        response.put("universityFullName", userProfile.getUniversityFullName());
        response.put("email", userProfile.getEmail());
        response.put("username", userProfile.getUsername());
        response.put("profileImageUrl", userProfile.getProfileImageUrl());
        response.put("uniscore", userProfile.getUniscore());
        response.put("roleId", userProfile.getRoleId());
        response.put("bio", userProfile.getBio());
        response.put("rank", userProfile.getRank());
        response.put("streak", userProfile.getStreak());
        response.put("totalNotes", userProfile.getTotalNotes());
        response.put("totalPublicNotes", userProfile.getTotalPublicNotes());
        response.put("totalLikes", userProfile.getTotalLikes());
        response.put("instagramUsername", userProfile.getInstagramUsername());
        response.put("seasonScore", userProfile.getSeasonScore());
        response.put("certified", userProfile.getCertified());
        response.put("subscriptionPlan", plan); // Use the newly found plan instead of userProfile.getSubscriptionPlan()
        response.put("freeTrialCompleted", userProfile.getFreeTrialCompleted());
        org.slf4j.LoggerFactory.getLogger(UserController.class).info("[getUserProfile] Returning plan for user {}: {}", userId, plan);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/info")
    public ResponseEntity<Map<String, Object>> getUserInfo(@PathVariable Long userId, @RequestParam(defaultValue = "EN") String language) {
        UserInfoProjection userInfo = userService.getUserInfo(userId, language);
        if (userInfo == null) {
            return ResponseEntity.notFound().build();
        }
        // Fetch latest active subscription plan
        String plan = "FREE";
        try {
            java.util.Optional<com.uninote.backend.entity.Subscription> opt = subscriptionService.findLatestActiveSubscriptionByUserId(userId);
            if (opt.isPresent() && opt.get().getPlan() != null) {
                plan = opt.get().getPlan().name();
            }
        } catch (Exception e) {
            // Log and default to FREE
            org.slf4j.LoggerFactory.getLogger(UserController.class).warn("Could not fetch subscription plan for user {}: {}", userId, e.getMessage());
        }
        // Build response map with all userInfo fields and plan
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("userId", userInfo.getUserId());
        response.put("username", userInfo.getUsername());
        response.put("profileImageUrl", userInfo.getProfileImageUrl());
        response.put("rankName", userInfo.getRankName());
        response.put("departmentId", userInfo.getDepartmentId());
        response.put("universityId", userInfo.getUniversityId());
        response.put("uniscore", userInfo.getUniscore());
        response.put("departmentName", userInfo.getDepartmentName());
        response.put("universityName", userInfo.getUniversityName());
        response.put("instagramUsername", userInfo.getInstagramUsername());
        response.put("seasonScore", userInfo.getSeasonScore());
        response.put("certified", userInfo.getCertified());
        response.put("freeTrialCompleted", userInfo.getFreeTrialCompleted());
        response.put("plan", plan);
        org.slf4j.LoggerFactory.getLogger(UserController.class).info("[getUserInfo] Returning plan for user {}: {}", userId, plan);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/rank/department")
    public Integer getUserRankInDepartment(@PathVariable Long userId) {
        return userService.getUserRankInDepartment(userId);
    }

    @GetMapping("/{userId}/rank/university")
    public Integer getUserRankInUniversity(@PathVariable Long userId) {
        return userService.getUserRankInUniversity(userId);
    }

    @GetMapping("/{userId}/rank/global")
    public Integer getUserGlobalRank(@PathVariable Long userId) {
        return userService.getUserGlobalRank(userId);
    }

    @GetMapping("/{userId}/ranks")
    public Map<String, Integer> getUserRanks(@PathVariable Long userId) {
        return userService.getUserRanks(userId);
    }

    @GetMapping("/{username}/userId")
    public Long getUserIdByUsername(@PathVariable String username) {
        return userService.getUserIdByUsername(username);
    }

    @GetMapping("/{username}/email")
    public String getUserEmailByUsername(@PathVariable String username) {
        return userService.getUserEmailByUsername(username);
    }

    @PostMapping("/verify-email/{firebaseUid}")
    public void verfiyEmail(@PathVariable String firebaseUid) {
        userService.verfiyEmail(firebaseUid);
    }

    @PostMapping("/free-trial")
    public ResponseEntity<?> createFreeTrial(@RequestBody MetadataRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>("Authorization token missing or invalid.", HttpStatus.UNAUTHORIZED);
        }

        FirebaseAuthentication firebaseAuth = (FirebaseAuthentication) authentication;
        String userUid = firebaseAuth.getUid();

        User user = userRepository.findByFirebaseUid(userUid)
                .orElseThrow(() -> new RuntimeException("User not found for Firebase UID: " + userUid));
        boolean hasFreeTrial = subscriptionService.existsByUserIdAndDuration(user.getId(), SubscriptionDuration.THREE_DAYS);
        if (hasFreeTrial) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("User has already used the free trial.");
        }
        Subscription subscription = subscriptionService.createFreeTrialSubscription(
                user.getId(),
                SubscriptionPlan.BASIC,
                request.getMetadata()
        );

        return ResponseEntity.ok(subscription);
    }

    @GetMapping("/free-trial-status")
    public ResponseEntity<FreeTrialStatusDTO> getFreeTrialStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        FirebaseAuthentication firebaseAuth = (FirebaseAuthentication) authentication;
        String userUid = firebaseAuth.getUid();

        User user = userRepository.findByFirebaseUid(userUid)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get the free trial subscription if it exists
        Optional<Subscription> freeTrialSub = subscriptionService.findByUser_IdAndDuration(user.getId(), SubscriptionDuration.THREE_DAYS);

        boolean freeTrialEnded = false;
        LocalDateTime freeTrialEndDate = null;
        boolean hasUsedChatAfterTrial = false;
        boolean hasUsedChatDuringTrial = false;
        boolean hasUsedFreeTrial = false;

        if (freeTrialSub.isPresent()) {
            Subscription trial = freeTrialSub.get();
            freeTrialEndDate = trial.getEndDate();
            freeTrialEnded = freeTrialEndDate != null && freeTrialEndDate.isBefore(LocalDateTime.now());

            // Check if user has used chat during trial period
            if (trial.getStartDate() != null && freeTrialEndDate != null) {
                hasUsedChatDuringTrial = messageRepository.countByChat_UserAndCreatedAtBetween(
                    user,
                    java.sql.Timestamp.valueOf(trial.getStartDate()),
                    java.sql.Timestamp.valueOf(freeTrialEndDate)
                ) > 0;
                // Set hasUsedFreeTrial based on whether they used chat during trial
                hasUsedFreeTrial = hasUsedChatDuringTrial;
            }

            // Check if user has used chat after trial ended
            if (freeTrialEnded) {
                hasUsedChatAfterTrial = messageRepository.existsByChat_UserAndCreatedAtAfter(
                    user,
                    java.sql.Timestamp.valueOf(freeTrialEndDate)
                );
            }
        }

        FreeTrialStatusDTO status = new FreeTrialStatusDTO(
            hasUsedFreeTrial,
            freeTrialEnded,
            hasUsedChatAfterTrial,
            freeTrialEndDate,
            hasUsedChatDuringTrial
        );

        return ResponseEntity.ok(status);
    }

    @GetMapping("/limits")
    public ResponseEntity<UserLimitsDTO> getUserLimits() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        FirebaseAuthentication firebaseAuth = (FirebaseAuthentication) authentication;
        String userUid = firebaseAuth.getUid();

        User user = userRepository.findByFirebaseUid(userUid)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserLimitsDTO limits = usageLimitService.getUserLimits(user);
        return ResponseEntity.ok(limits);
    }

    @PostMapping("/onboarding-data")
    public ResponseEntity<Boolean> setOnboardingData(@RequestBody Object onboardingData) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        FirebaseAuthentication firebaseAuth = (FirebaseAuthentication) authentication;
        String userUid = firebaseAuth.getUid();

        try {
            // Convert the Object to JSON string
            ObjectMapper mapper = new ObjectMapper();
            String onboardingDataString = mapper.writeValueAsString(onboardingData);
            boolean success = userService.setOnboardingData(userUid, onboardingDataString);
            return ResponseEntity.ok(success);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }

    @GetMapping("/onboarded")
    public ResponseEntity<Map<String, Boolean>> isOnboarded() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        FirebaseAuthentication firebaseAuth = (FirebaseAuthentication) authentication;
        String userUid = firebaseAuth.getUid();

        boolean isOnboarded = userService.isOnboarded(userUid);
        Map<String, Boolean> response = new java.util.HashMap<>();
        response.put("onboarded", isOnboarded);
        return ResponseEntity.ok(response);
    }
}
