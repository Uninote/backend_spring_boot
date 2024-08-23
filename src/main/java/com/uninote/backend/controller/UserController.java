package com.uninote.backend.controller;

import com.uninote.backend.dto.UserDTO;
import com.uninote.backend.dto.UserStatsDTO;
import com.uninote.backend.converter.EntityToDTOConverter;
import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.University;
import com.uninote.backend.entity.User;
import com.uninote.backend.interfaceProjection.UserProfileProjection;
import com.uninote.backend.repository.DepartmentRepository;
import com.uninote.backend.repository.UniversityRepository;
import com.uninote.backend.service.UserService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UniversityRepository universityRepository;

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
    public ResponseEntity<List<UserDTO>> getLeaderboard(){
        List <UserDTO> leaderborad = userService.getTop100UsersByUniscore();
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
    public ResponseEntity<List<UserDTO>> getLeaderboardByUniversity(@PathVariable Long universityId) {
        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new IllegalArgumentException("University not found"));
        List<UserDTO> leaderboard = userService.getTop100UsersByUniscoreByUniversity(university);
        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/leaderboard/department/{departmentId}")
    public ResponseEntity<List<UserDTO>> getLeaderboardByDepartment(@PathVariable Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        List<UserDTO> leaderboard = userService.getTop100UsersByUniscoreByDepartment(department);
        return ResponseEntity.ok(leaderboard);
    }

    @PutMapping("/{id}/login")
    public ResponseEntity<User> loginUserAndUpdateStreak(@PathVariable Long id) {
        User updatedUser = userService.loginUserAndUpdateStreak(id);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/firebase/{firebaseUid}")
    public ResponseEntity<Long> findByFirebaseUid(@PathVariable String firebaseUid) {
        Long userId = userService.findByFirebaseUid(firebaseUid);
        return userId!= null ? ResponseEntity.ok(userId) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
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
            userDTO.setDepartmentId(user.getDepartment().getId());
            userDTO.setUniversityId(user.getUniversity().getId());
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
    public ResponseEntity<UserProfileProjection> getUserProfile(
            @PathVariable Long userId, 
            @RequestParam Long languageId) {
        UserProfileProjection userProfile = userService.getUserProfileById(userId, languageId);
        return ResponseEntity.ok(userProfile);
    }
}
