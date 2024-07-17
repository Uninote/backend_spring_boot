package com.uninote.backend.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.uninote.backend.entity.User;


public interface UserRepository extends JpaRepository<User, Long> {
}