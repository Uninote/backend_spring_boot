package com.uninote.backend.repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uninote.backend.dto.SimpleChatSummaryDTO;
import com.uninote.backend.entity.Chat;
import com.uninote.backend.entity.User;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    
    List<Chat> findByUserOrderByUpdatedAtDesc(User user);
    
    Optional<Chat> findByUuid(String uuid);
    
    Optional<Chat> findByIdAndUser(Long id, User user);
    
    List<Chat> findByUserAndTitleContainingIgnoreCaseOrderByUpdatedAtDesc(User user, String searchTerm);
    
    void deleteByIdAndUser(Long id, User user);

    long countByUserAndCreatedAtBetween(User user, Date startOfDay, Date endOfDay);

    @Query("SELECT new com.uninote.backend.dto.SimpleChatSummaryDTO(" +
        "c.id, c.uuid, c.createdAt, c.title) " +
        "FROM Chat c " +
        "WHERE c.user = :user " +
        "AND c.id NOT IN (SELECT rc.chat.id FROM ResourceChat rc) " +
        "AND c.id NOT IN (SELECT sc.id FROM SpaceChat sc) " +
        "ORDER BY c.createdAt DESC")
    List<SimpleChatSummaryDTO> findSimpleChatSummariesByUser(@Param("user") User user);



}