package com.uninote.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uninote.backend.entity.LikeHistory;

@Repository
public interface LikeHistoryRepository extends JpaRepository<LikeHistory, Long> {
}
