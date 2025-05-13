package com.uninote.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uninote.backend.entity.Chat;
import com.uninote.backend.entity.Message;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Date;

import com.uninote.backend.dto.MessageDTO;
import com.uninote.backend.entity.Chat;
import com.uninote.backend.entity.Message;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.ChatRepository;
import com.uninote.backend.repository.MessageRepository;
import com.uninote.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByChatId(Long chatId);

    List<Message> findByChatOrderByCreatedAtAsc(Chat chat);

    @Query("SELECT m FROM Message m LEFT JOIN FETCH m.media WHERE m.chat.id = :chatId ORDER BY m.createdAt ASC")
    List<Message> findAllByChatIdOrderByCreatedAtAsc(@Param("chatId") Long chatId);
    Page<Message> findByChatOrderByCreatedAtDesc(Chat chat, Pageable pageable);
    
    void deleteByChatId(Long chatId);
    
    List<Message> findByChatIdAndUserMessageContainingIgnoreCase(Long chatId, String searchTerm);


    int countByChat(Chat baseChat);

    long countByChat_UserAndCreatedAtBetween(User user, Date start, Date end);

}