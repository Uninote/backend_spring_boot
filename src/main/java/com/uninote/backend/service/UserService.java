package com.uninote.backend.service;

import com.uninote.backend.converter.EntityToDTOConverter;
import com.uninote.backend.dto.UserDTO;
import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.Rank;
import com.uninote.backend.entity.UniscoreIncreaseLog;
import com.uninote.backend.entity.UniscoreIncreaseType;
import com.uninote.backend.entity.University;
import com.uninote.backend.entity.User;
import com.uninote.backend.entity.UserLogin;
import com.uninote.backend.repository.DepartmentRepository;
import com.uninote.backend.repository.RankRepository;
import com.uninote.backend.repository.UniscoreIncreaseLogRepository;
import com.uninote.backend.repository.UniscoreIncreaseTypeRepository;
import com.uninote.backend.repository.UniversityRepository;
import com.uninote.backend.repository.UserLoginRepository;
import com.uninote.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private final DepartmentRepository departmentRepository = null;
    
    @Autowired
    private final UniversityRepository universityRepository = null;
    
    @Autowired
    private final RankRepository rankRepository = null;

    @Autowired
    private UserLoginRepository userLoginRepository;

    @Autowired
    private BadgeService badgeService;

    @Autowired
    private UniscoreIncreaseTypeRepository uniScoreIncreaseTypeRepository;

    @Autowired
    private UniscoreIncreaseLogRepository uniscoreIncreaseLogRepository;

    public User loginUserAndUpdateStreak(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        LocalDate lastLoginDate = (user.getLastLogin() != null) ? user.getLastLogin().toLocalDate() : null;
        LocalDate today = LocalDate.now();

        if (lastLoginDate == null || lastLoginDate.isBefore(today.minusDays(1))) {
            user.setStreak(1);
        } else if (lastLoginDate.isEqual(today.minusDays(1))) {
            user.setStreak(user.getStreak() + 1);
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        UserLogin userLogin = new UserLogin();
        userLogin.setUser(user);
        userLogin.setLoginTimestamp(LocalDateTime.now());
        userLoginRepository.save(userLogin);

        return user;
    }

    public User findById(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        return userOptional.orElse(null);
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

        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public User getUserById(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        return userOptional.orElse(null);
    }
    

    public List<UserDTO> getTop100UsersByUniscore(){
        List<User> topUsers = userRepository.findTop100ByUniscore();
        return topUsers.stream().map(EntityToDTOConverter::convertUserToDTO).collect(Collectors.toList());
    }

    public List<UserDTO> getTop100UsersByUniscoreByDepartment(Department department){
        List<User> topUsers = userRepository.findTop100ByUniscoreByDepartment(department);
        return topUsers.stream().map(EntityToDTOConverter::convertUserToDTO).collect(Collectors.toList());
    }
    public List<UserDTO> getTop100UsersByUniscoreByUniversity(University university){
        List<User> topUsers = userRepository.findTop100ByUniscoreByUniversity(university);
        return topUsers.stream().map(EntityToDTOConverter::convertUserToDTO).collect(Collectors.toList());
    }

    
    public User updateStreakOnLogin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        LocalDate lastLoginDate = (user.getLastLogin() != null) ? user.getLastLogin().toLocalDate() : null;
        LocalDate today = LocalDate.now();

        if (lastLoginDate == null || lastLoginDate.isBefore(today.minusDays(1))) {
           
            user.setStreak(1);
        } else if (lastLoginDate.isEqual(today.minusDays(1))) {
           
            user.setStreak(user.getStreak() + 1);
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        badgeService.checkBadgesForUser(userId);
        return user;
    }

    @Transactional
    public void updateUniScore(User user, Long activityType) {
        UniscoreIncreaseType uniScoreIncreaseType = uniScoreIncreaseTypeRepository.findById(activityType).orElseThrow(() -> new IllegalArgumentException("Invalid increase Type"));
        if (uniScoreIncreaseType != null) {
            user.setUniscore(user.getUniscore() + uniScoreIncreaseType.getIncreaseAmount());
            UniscoreIncreaseLog increaseLog = new UniscoreIncreaseLog(user, uniScoreIncreaseType);
            uniscoreIncreaseLogRepository.save(increaseLog);
            userRepository.save(user);
        } else {
            throw new IllegalArgumentException("Unknown activity type: " + activityType);
        }
    }

    public UserDTO findByFirebaseUid(String firebaseUid) {
        Optional<User> userOptional = userRepository.findByFirebaseUid(firebaseUid);
        if (userOptional.isPresent()) {
            return EntityToDTOConverter.convertUserToDTO(userOptional.get());
        } else {
            return null;
        }
    }
}