package com.uninote.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uninote.backend.entity.Role;

public interface RoleRepository extends JpaRepository<Role,Long>{
    Optional<Role> findById(Long id);
}
