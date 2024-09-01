package com.uninote.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uninote.backend.entity.BadgeNotification;

@Repository
public interface BadgeNotificationRepository extends JpaRepository<BadgeNotification, Long> {
    List<BadgeNotification> findByUserIdAndDeliveredFalse(Long userId);
}