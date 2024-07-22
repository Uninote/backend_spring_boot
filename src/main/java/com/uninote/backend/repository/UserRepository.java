package com.uninote.backend.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.User;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(String id);
}