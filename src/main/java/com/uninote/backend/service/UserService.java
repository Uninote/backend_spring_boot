package com.uninote.backend.service;

import com.uninote.backend.dto.UserDTO;
import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.Rank;
import com.uninote.backend.entity.University;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.DepartmentRepository;
import com.uninote.backend.repository.RankRepository;
import com.uninote.backend.repository.UniversityRepository;
import com.uninote.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    public User updateUser(Long userId, User userDetails) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setFirebaseUid(userDetails.getFirebaseUid());
            user.setStreak(userDetails.getStreak());
            user.setUniscore(userDetails.getUniscore());
            user.setUpdatedAt(userDetails.getUpdatedAt());
            user.setLastLogin(userDetails.getLastLogin());
            user.setName(userDetails.getName());
            user.setDepartment(userDetails.getDepartment());
            user.setUniversity(userDetails.getUniversity());
            user.setEmail(userDetails.getEmail());
            user.setNoteClicks(userDetails.getNoteClicks());
            user.setRank(userDetails.getRank());
            user.setUsername(userDetails.getUsername());
            user.setProfileImageUrl(userDetails.getProfileImageUrl());
            user.setNotesNumber(userDetails.getNotesNumber());
            user.setPublicNotesNumber(userDetails.getPublicNotesNumber());
            return userRepository.save(user);
        }
        return null;
    }

    public User getUserById(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        return userOptional.orElse(null);
    }

    
}
