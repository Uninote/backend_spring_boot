package com.uninote.backend.controller;

import com.uninote.backend.dto.UserDTO;
import com.uninote.backend.converter.EntityToDTOConverter;
import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.University;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.DepartmentRepository;
import com.uninote.backend.repository.UniversityRepository;
import com.uninote.backend.service.UserService;

import java.util.List;

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

    @PutMapping("/{userId}/login")
    public ResponseEntity<UserDTO> updateStreakOnLogin(@PathVariable Long userId) {
        try {
            User updatedUser = userService.updateStreakOnLogin(userId);
            UserDTO updatedUserDTO = EntityToDTOConverter.convertUserToDTO(updatedUser);
            return ResponseEntity.ok(updatedUserDTO);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    
}
