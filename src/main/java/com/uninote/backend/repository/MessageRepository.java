package com.uninote.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uninote.backend.entity.Chat;
import com.uninote.backend.entity.Message;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByChatId(Long chatId);

    List<Message> findByChatOrderByCreatedAtAsc(Chat chat);
    List<Message> findAllByChatIdOrderByCreatedAtAsc(Long chatId);

}