package com.uninote.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uninote.backend.entity.PromptVariant;

public interface PromptVariantRepository extends JpaRepository<PromptVariant, Long> {
    Optional<PromptVariant> findByName(String name);
    List<PromptVariant> findByActiveTrue();
} 