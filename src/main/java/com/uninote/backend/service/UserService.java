package com.uninote.backend.service;

import com.uninote.backend.controller.LoginWebSocketController;
import com.uninote.backend.converter.EntityToDTOConverter;
import com.uninote.backend.dto.UserDTO;
import com.uninote.backend.dto.UserStatsDTO;
import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.Rank;
import com.uninote.backend.entity.Role;
import com.uninote.backend.entity.UniscoreIncreaseLog;
import com.uninote.backend.entity.UniscoreIncreaseType;
import com.uninote.backend.entity.University;
import com.uninote.backend.entity.User;
import com.uninote.backend.entity.UserLogin;
import com.uninote.backend.interfaceProjection.UserInfoProjection;
import com.uninote.backend.interfaceProjection.UserProfileProjection;
import com.uninote.backend.repository.DepartmentRepository;
import com.uninote.backend.repository.RankRepository;
import com.uninote.backend.repository.RoleRepository;
import com.uninote.backend.repository.UniscoreIncreaseLogRepository;
import com.uninote.backend.repository.UniscoreIncreaseTypeRepository;
import com.uninote.backend.repository.UniversityRepository;
import com.uninote.backend.repository.UserLoginRepository;
import com.uninote.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RankService rankService;

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
    private UniscoreIncreaseTypeRepository uniScoreIncreaseTypeRepository;

    @Autowired
    private UniscoreIncreaseLogRepository uniscoreIncreaseLogRepository;

    @Autowired
    private LoginWebSocketController loginWebSocketController;

    public Void loginUserAndUpdateStreak(Long userId) {
        Boolean eligibleForUniscore = false;
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        LocalDate lastLoginDate = (user.getLastLogin() != null) ? user.getLastLogin().toLocalDate() : null;
        LocalDate today = LocalDate.now();

        if (lastLoginDate == null || lastLoginDate.isBefore(today.minusDays(1))) {
            user.setStreak(1);
            user.setLastLogin(LocalDateTime.now());
            updateUniScore(user, 24L);
            eligibleForUniscore = true;
        } else if (lastLoginDate.isEqual(today.minusDays(1))) {
            user.setStreak(user.getStreak() + 1);
            user.setLastLogin(LocalDateTime.now());
            updateUniScore(user, 24L);
            updateUniScore(user, 25L);
            eligibleForUniscore = true;
        } else if (lastLoginDate.isEqual(today)) {
            user.setLastLogin(LocalDateTime.now());
        }

        if (eligibleForUniscore) {
            loginWebSocketController.sendLoginNotification(user.getFirebaseUid(), "Congratulations! You have received 50 uniscore for logging in today.");
        }
        
        userRepository.save(user);

        UserLogin userLogin = new UserLogin();  
        userLogin.setUser(user);
        userLogin.setLoginTimestamp(LocalDateTime.now());
        userLoginRepository.save(userLogin);
        badgeService.checkBadgesForUser(userId);
        return null;
    }

    public User findById(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        return userOptional.orElse(null);
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
        } else {
            throw new IllegalArgumentException("User not found with ID: " + userId);
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
        user.setLastLogin(LocalDateTime.now());
        user.setRole(role);

        
        return userRepository.save(user);
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

    public UserProfileProjection getUserProfileById(Long userId, Long languageId) {
        return userRepository.findUserProfileById(userId, languageId);
    }


     public UserInfoProjection getUserInfo(Long userId) {
        return userRepository.findUserInfoById(userId,1L);
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

}