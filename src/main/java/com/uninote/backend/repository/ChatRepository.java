package com.uninote.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uninote.backend.entity.Chat;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    Chat findByUuid(String uuid);
}