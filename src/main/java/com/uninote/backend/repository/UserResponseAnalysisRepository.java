package com.uninote.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uninote.backend.entity.UserResponseAnalysis;

@Repository
public interface UserResponseAnalysisRepository extends JpaRepository<UserResponseAnalysis, Long> {
    
    @Query("SELECT ura FROM UserResponseAnalysis ura WHERE ura.user.id = :userId ORDER BY ura.createdAt DESC")
    UserResponseAnalysis findLatestAnalysis(@Param("userId") Long userId);
} 