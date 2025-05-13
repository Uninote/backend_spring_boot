package com.uninote.backend.repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}