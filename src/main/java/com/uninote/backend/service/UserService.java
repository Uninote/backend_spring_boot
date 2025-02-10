package com.uninote.backend.service;

import com.uninote.backend.controller.LoginWebSocketController;
import com.uninote.backend.converter.EntityToDTOConverter;
import com.uninote.backend.dto.UserDTO;
import com.uninote.backend.dto.UserStatsDTO;
import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.Rank;
import com.uninote.backend.entity.Role;
import com.uninote.backend.entity.Season;
import com.uninote.backend.entity.UniscoreIncreaseLog;
import com.uninote.backend.entity.UniscoreIncreaseType;
import com.uninote.backend.entity.University;
import com.uninote.backend.entity.User;
import com.uninote.backend.entity.UserLogin;
import com.uninote.backend.entity.UserSeasonPoints;
import com.uninote.backend.entity.UserSeasonPointsId;
import com.uninote.backend.entity.UserSession;
import com.uninote.backend.interfaceProjection.UserInfoProjection;
import com.uninote.backend.interfaceProjection.UserProfileProjection;
import com.uninote.backend.repository.CommentLikeRepository;
import com.uninote.backend.repository.CommentRepository;
import com.uninote.backend.repository.DepartmentRepository;
import com.uninote.backend.repository.NoteCollectionRepository;
import com.uninote.backend.repository.NoteLikeRepository;
import com.uninote.backend.repository.NoteRepository;
import com.uninote.backend.repository.NoteSaveRepository;
import com.uninote.backend.repository.RankRepository;
import com.uninote.backend.repository.RoleRepository;
import com.uninote.backend.repository.UniscoreIncreaseLogRepository;
import com.uninote.backend.repository.UniscoreIncreaseTypeRepository;
import com.uninote.backend.repository.UniversityRepository;
import com.uninote.backend.repository.UserCourseGradeRepository;
import com.uninote.backend.repository.UserLoginRepository;
import com.uninote.backend.repository.UserRepository;
import com.uninote.backend.repository.UserSeasonPointsRepository;
import com.uninote.backend.repository.UserSessionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PrimitiveIterator;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;



@Service
public class UserService {

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCourseGradeRepository userCourseGradeRepository;

    @Autowired
    private RankService rankService;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private NoteLikeRepository noteLikeRepository;

    @Autowired
    private NoteSaveRepository noteSaveRepository;

    @Autowired
    private final DepartmentRepository departmentRepository = null;
    
    @Autowired
    private final UniversityRepository universityRepository = null;
    
    @Autowired
    private final RankRepository rankRepository = null;

    @Autowired
    private UserLoginRepository userLoginRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BadgeService badgeService;


    @Autowired
    private UserSeasonPointsRepository userSeasonPointsRepository;

    @Autowired
    private UniscoreIncreaseTypeRepository uniScoreIncreaseTypeRepository;

    @Autowired
    private UniscoreIncreaseLogRepository uniscoreIncreaseLogRepository;

    @Autowired
    private LoginWebSocketController loginWebSocketController;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private NoteCollectionRepository noteCollectionRepository;  

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private SeasonService seasonService;


    @Autowired
    private MixPanelService mixPanelService;


    private final Map<Long, Object> locks = new ConcurrentHashMap<>();
    

    public Long getTotalUsers() {
        return userRepository.countTotalVerifiedUsers();
    }

    public Long getTotalUnverifiedUsers() {
        return userRepository.countTotalUnverifiedUsers();
    }

    public Long loginUserAndUpdateStreak(Long userId, String anonymusSessionId) {

        locks.putIfAbsent(userId, new Object());
        synchronized (locks.get(userId)) {
            try {

                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

                LocalDate lastLoginDate = (user.getLastLogin() != null) ? user.getLastLogin().toLocalDate() : null;
                LocalDate today = LocalDate.now();


                boolean isFirstLogin = (lastLoginDate == null || lastLoginDate.isBefore(today.minusDays(1)));
                boolean isConsecutiveLogin = (lastLoginDate != null && lastLoginDate.isEqual(today.minusDays(1)));


                if (isFirstLogin) {
                    user.setStreak(1);
                    updateUniScore(user, 24L);
                } else if (isConsecutiveLogin) {
                    user.setStreak(user.getStreak() + 1);
                    updateUniScore(user, 24L);
                    updateUniScore(user, 25L);
                }


                user.setLastLogin(LocalDateTime.now());
                CompletableFuture<Void> notificationFuture = CompletableFuture.runAsync(()-> {
                    if (isFirstLogin || isConsecutiveLogin) {
                        loginWebSocketController.sendLoginNotification(
                                user.getFirebaseUid(),
                                "Congratulations! You have received 50 uniscore for logging in today."
                        );
                    }
                });
                userRepository.save(user);

                CompletableFuture<Void> badgeFuture = CompletableFuture.runAsync(() -> {
                    badgeService.checkBadgesForUser(userId);

                });

                CompletableFuture<Void> mixpanelFuture = CompletableFuture.runAsync(() -> {
                    //mixPanelService.identifyUser(anonymusSessionId, userId);

                });
                UserLogin userLogin = new UserLogin();  
                userLogin.setUser(user);
                userLogin.setLoginTimestamp(LocalDateTime.now());
                userLoginRepository.save(userLogin);
                Optional<UserSession> activeSession = userSessionRepository.findActiveSessionByUserId(userId);
                Long sessionId;
                if (activeSession.isPresent()) {
                
                    sessionId = activeSession.get().getSessionId();
                } else {
                    sessionId = userSessionService.startSession(userId);
                }


                CompletableFuture<Void> allTasks = CompletableFuture.allOf(notificationFuture,badgeFuture, mixpanelFuture);
                allTasks.exceptionally(ex -> {
                    System.err.println("An error occurred during asynchronous operations: " + ex.getMessage());
                    return null;
                });
                //allTasks.join();
                return sessionId;
            } finally {
                locks.remove(userId);
            }
        }
    }


    public Long loginUserAndUpdateStreak(Long userId, Boolean deviceId) {
        // 0 for laptop
        //1 for phone
        locks.putIfAbsent(userId, new Object());
        synchronized (locks.get(userId)) {
            try {

                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

                LocalDate lastLoginDate = (user.getLastLogin() != null) ? user.getLastLogin().toLocalDate() : null;
                LocalDate today = LocalDate.now();


                boolean isFirstLogin = (lastLoginDate == null || lastLoginDate.isBefore(today.minusDays(1)));
                boolean isConsecutiveLogin = (lastLoginDate != null && lastLoginDate.isEqual(today.minusDays(1)));


                if (isFirstLogin) {
                    user.setStreak(1);
                    updateUniScore(user, 24L);
                } else if (isConsecutiveLogin) {
                    user.setStreak(user.getStreak() + 1);
                    updateUniScore(user, 24L);
                    updateUniScore(user, 25L);
                }


                user.setLastLogin(LocalDateTime.now());
                CompletableFuture<Void> notificationFuture = CompletableFuture.runAsync(()-> {
                    if (isFirstLogin || isConsecutiveLogin) {
                        loginWebSocketController.sendLoginNotification(
                                user.getFirebaseUid(),
                                "Congratulations! You have received 50 uniscore for logging in today."
                        );
                    }
                });
                userRepository.save(user);

                CompletableFuture<Void> badgeFuture = CompletableFuture.runAsync(() -> {
                    badgeService.checkBadgesForUser(userId);

                });

                UserLogin userLogin = new UserLogin();  
                userLogin.setUser(user);
                userLogin.setLoginTimestamp(LocalDateTime.now());
                userLogin.setDevice(deviceId);
                userLoginRepository.save(userLogin);
                Optional<UserSession> activeSession = userSessionRepository.findActiveSessionByUserId(userId);
                Long sessionId;
                if (activeSession.isPresent()) {
                
                    sessionId = activeSession.get().getSessionId();
                } else {
                    sessionId = userSessionService.startSession(userId);
                }


                CompletableFuture<Void> allTasks = CompletableFuture.allOf(notificationFuture,badgeFuture);
                allTasks.exceptionally(ex -> {
                    System.err.println("An error occurred during asynchronous operations: " + ex.getMessage());
                    return null;
                });
                //allTasks.join();
                return sessionId;
            } finally {
                locks.remove(userId);
            }
        }
    }

    public User findById(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        return userOptional.orElse(null);
    }

    @Transactional
public void softDeleteUserById(Long userId) {
    try {

        List<Long> commentIds = commentRepository.findCommentIdsByUserId(userId);
        if (!commentIds.isEmpty()) {
            commentLikeRepository.deleteByComment_CommentIdIn(commentIds);
        }
        commentLikeRepository.deleteByUserId(userId);
        commentRepository.deleteByUserId(userId);

        
        noteRepository.softDeleteByUserId(userId);

        

        noteCollectionRepository.softDeleteCollectionsByUserId(userId);
       
        noteLikeRepository.setInactiveByUserId(userId);

       
        noteSaveRepository.setInactiveByUserId(userId);
        
    
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        user.setUsername(null);
        user.setFirebaseUid(null);
        user.setEmail(null);
        user.setName(null);
        Role role = roleRepository.findById(21L). orElseThrow(() -> new IllegalArgumentException("Role with id 4 not found"));
        user.setRole(role);
        userRepository.save(user);

    } catch (Exception e) {
        
        throw new RuntimeException("Failed to delete user", e);
    }
}

    public User saveUser(User user) {
        return userRepository.save(user);
    }
    public User createUser(UserDTO userDto) {
        
        if (userDto.getFirebaseUid() == null || userDto.getFirebaseUid().isEmpty()) {
            throw new IllegalArgumentException("Firebase UID must not be null or empty");
        }
        if (userDto.getName() == null || userDto.getName().isEmpty()) {
            throw new IllegalArgumentException("Name must not be null or empty");
        }
        if (userDto.getDepartmentId() == null) {
            throw new IllegalArgumentException("Department ID must not be null");
        }
        if (userDto.getUniversityId() == null) {
            throw new IllegalArgumentException("University ID must not be null");
        }
        if (userDto.getEmail() == null || userDto.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email must not be null or empty");
        }
        if (userDto.getUsername() == null || userDto.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username must not be null or empty");
        }

        Department department = departmentRepository.findById(userDto.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid department ID: " + userDto.getDepartmentId()));

        
        University university = universityRepository.findById(userDto.getUniversityId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid university ID: " + userDto.getUniversityId()));

        
        Rank defaultRank = rankRepository.findById(1L)
                .orElseThrow(() -> new IllegalStateException("Default rank not found"));

        Role role = roleRepository.findById(userDto.getRoleId()).orElseThrow(() -> new IllegalArgumentException("Role not found"));
        
        User user = new User();
        user.setFirebaseUid(userDto.getFirebaseUid());
        user.setName(userDto.getName());
        user.setDepartment(department);
        user.setUniversity(university);
        user.setEmail(userDto.getEmail());
        user.setUsername(userDto.getUsername());
        user.setProfileImageUrl(userDto.getProfileImageUrl());
        user.setRank(defaultRank);
        user.setUpdatedAt(LocalDateTime.now());
        //user.setLastLogin(LocalDateTime.now());
        user.setRole(role);
        User savedUser = userRepository.save(user);
        Optional<Season> currentSeasonOpt = seasonService.getCurrentSeason();
        if (currentSeasonOpt.isPresent()) {
            Season currentSeason = currentSeasonOpt.get();

            UserSeasonPointsId userSeasonPointsId =new UserSeasonPointsId();
            userSeasonPointsId.setSeasonId(currentSeason.getSeasonId());
            userSeasonPointsId.setUserId(user.getId());
            UserSeasonPoints userSeasonPoints = new UserSeasonPoints(userSeasonPointsId, 0, null, false);
            userSeasonPointsRepository.save(userSeasonPoints);
        }
        
        return savedUser;
    }
    public User updateUser(Long userId, UserDTO userDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        if (userDto.getFirebaseUid() != null) {
            user.setFirebaseUid(userDto.getFirebaseUid());
        }
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(userDto.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid department ID: " + userDto.getDepartmentId()));
            user.setDepartment(department);
        }
        if (userDto.getUniversityId() != null) {
            University university = universityRepository.findById(userDto.getUniversityId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid university ID: " + userDto.getUniversityId()));
            user.setUniversity(university);
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getUsername() != null) {
            user.setUsername(userDto.getUsername());
        }
        if (userDto.getProfileImageUrl() != null) {
            user.setProfileImageUrl(userDto.getProfileImageUrl());
        }
        if(userDto.getBio() !=null) {
            user.setBio(userDto.getBio());
        }
        if(userDto.getInstagramUsername() != null) {
            user.setInstagramUsername(userDto.getInstagramUsername());
        }

        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public User getUserById(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        return userOptional.orElse(null);
    }
    

    public List<UserInfoProjection> getTop100UsersByUniscore(){
        return userRepository.findTop100ByUniscore();
        
    }

    public List<UserInfoProjection> getTop100UsersByUniscoreByDepartment(Long departmentId){
        return userRepository.findTop100ByUniscoreByDepartment(departmentId);
        
    }
    public List<UserInfoProjection> getTop100UsersByUniscoreByUniversity(Long universityId){
        return userRepository.findTop100ByUniscoreByUniversity(universityId);
       
    }

    
    

    @Transactional
    public void updateUniScore(User user, Long activityType) {
        UniscoreIncreaseType uniScoreIncreaseType = uniScoreIncreaseTypeRepository.findById(activityType).orElseThrow(() -> new IllegalArgumentException("Invalid increase Type"));
        if (uniScoreIncreaseType != null) {
            user.setUniscore(user.getUniscore() + uniScoreIncreaseType.getIncreaseAmount());
            Rank newRank = rankService.determineRank(user.getUniscore());
            user.setRank(newRank);
            UniscoreIncreaseLog increaseLog = new UniscoreIncreaseLog(user, uniScoreIncreaseType);
            uniscoreIncreaseLogRepository.save(increaseLog);
            Optional<Season> currentSeasonOpt = seasonService.getCurrentSeason();
            if (currentSeasonOpt.isPresent()) {
                Season currentSeason = currentSeasonOpt.get();

                UserSeasonPoints userSeasonPoints;
                Long userId = user.getId();
                Long seasonId = currentSeason.getSeasonId();
                Optional<UserSeasonPoints> userSeasonPointsOpt = userSeasonPointsRepository.findByIdUserIdAndIdSeasonId(userId, seasonId);
                if(!userSeasonPointsOpt.isPresent()) {
                    UserSeasonPointsId userSeasonPointsId =new UserSeasonPointsId();
                    userSeasonPointsId.setSeasonId(currentSeason.getSeasonId());
                    userSeasonPointsId.setUserId(user.getId());
                    userSeasonPoints = new UserSeasonPoints(userSeasonPointsId, 0, null, false);
                    
                } else{
                    userSeasonPoints = userSeasonPointsOpt.get();
                }
                if(user.getSeasonScore() != null) {
                    user.setSeasonScore(user.getSeasonScore() + uniScoreIncreaseType.getIncreaseAmount());
                } else {
                    user.setSeasonScore((long)uniScoreIncreaseType.getIncreaseAmount());

                }

                userRepository.save(user);

                userSeasonPoints.setPoints(userSeasonPoints.getPoints() + uniScoreIncreaseType.getIncreaseAmount());

                userSeasonPointsRepository.save(userSeasonPoints);
            }
                userRepository.save(user);
                badgeService.checkBadgesForUser(user.getId());
        } else {
            throw new IllegalArgumentException("Unknown activity type: " + activityType);
        }
    }

    public Long findByFirebaseUid(String firebaseUid) {
        Optional<Long> userId = userRepository.findUserIdByFirebaseUid(firebaseUid);
        if (userId.isPresent()) {
            return userId.get();
        } else {
            return null;
        }
    }

    @Transactional
    public User approveUser(Long userId, Long approvedUserId){
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        User approvedUser = userRepository.findById(approvedUserId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.getApprovedUsers().add(approvedUser);
        userRepository.save(user);
        approvedUser.getApprovedByUsers().add(user);
        userRepository.save(approvedUser);
        return user;
    }

    @Transactional
    public Set<User> getApprovedUsers(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return user.getApprovedUsers();
    }

    @Transactional
    public Set<User> getUsersWhoApproved(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return user.getApprovedByUsers();
    }

     public UserStatsDTO getUserStats(Long userId) {
        long totalNotes = userRepository.countUserNotes(userId);
        long totalPublicNotes = userRepository.countUserPublicNotes(userId);
        long totalLikes = userRepository.countUserLikes(userId);

        return new UserStatsDTO(totalNotes, totalPublicNotes, totalLikes);
    }


    @Transactional(readOnly = true)
    public boolean doesUsernameExist(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean doesEmailExist(String email) {
        return userRepository.existsByEmail(email);
    }

    public UserProfileProjection getUserProfileById(Long userId, String language) {
        return userRepository.findUserProfileById(userId, language);
    }


     public UserInfoProjection getUserInfo(Long userId, String language) {
        return userRepository.findUserInfoById(userId,language);
    }

    
    public Integer getUserRankInDepartment(Long userId) {
        return userRepository.findUserRankInDepartment(userId);
    }

    
    public Integer getUserRankInUniversity(Long userId) {
        return userRepository.findUserRankInUniversity(userId);
    }

    
    public Integer getUserGlobalRank(Long userId) {
        return userRepository.findUserGlobalRank(userId);
    }

    public Map<String, Integer> getUserRanks(Long userId) {
        Map<String, Integer> userRanks = new HashMap<>();

        
        Integer rankInDepartment = userRepository.findUserRankInDepartment(userId);
        userRanks.put("department", rankInDepartment);

        Integer rankInUniversity = userRepository.findUserRankInUniversity(userId);
        userRanks.put("university", rankInUniversity);

        
        Integer globalRank = userRepository.findUserGlobalRank(userId);
        userRanks.put("global", globalRank);

        return userRanks;
    }



    public Long getUserIdByUsername(String username) {
        return userRepository.findUserIdByUsername(username)
                             .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }


    public String getUserEmailByUsername(String username) {
        return userRepository.findUserEmailByUsername(username)
                             .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }


    @Async
    public Long logoutUserAndEndSession(Long userId) {
        Optional<UserSession> activeSessionOpt = userSessionRepository.findLastActiveSessionByUserId(userId);

        if (activeSessionOpt.isEmpty()) {
            throw new IllegalArgumentException("No active session found for the user.");
        }

        UserSession session = activeSessionOpt.get();

        session.setLogoutTime(LocalDateTime.now()); 
        session.setSessionStatus(false);  
        userSessionRepository.save(session);  

        return session.getSessionId();  
    }



    public void verfiyEmail(String firebaseUuid) {
        Long id = userRepository.findUserIdByFirebaseUid(firebaseUuid).orElseThrow(() -> new IllegalArgumentException("Firebase uuid not found"));
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    public void ceritfyUser(Long userId) {
        if (noteRepository.countByUserId(userId) >= 20 && userCourseGradeRepository.countByUserId(userId).compareTo(BigDecimal.ONE) >= 0) {
            User user =  userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
            user.setCertified(true);
            userRepository.save(user);
        }
    }

    public Long getDeletedUsers() {
       return userRepository.getTotalDeletedUsers();
    }
}