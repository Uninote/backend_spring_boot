package com.uninote.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uninote.backend.entity.EmailEvent;

@Repository
public interface EmailEventRepository extends JpaRepository<EmailEvent, Long> {
   boolean existsByUserIdAndConditionSql(Long userId, String conditionSql);
} 