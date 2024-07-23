package com.uninote.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uninote.backend.entity.BadgeType;

import java.util.Optional;

public interface BadgeTypeRepository extends JpaRepository<BadgeType, Long> {
    Optional<BadgeType> findByName(String name);
}