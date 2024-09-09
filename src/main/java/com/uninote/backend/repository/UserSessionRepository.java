package com.uninote.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uninote.backend.entity.UserSession;

@Repository
public interface UserSessionRepository  extends JpaRepository<UserSession, Long>{

    @Query("SELECT us.sessionId FROM UserSession us WHERE us.userId = :userId ORDER BY us.loginTime DESC")
    Optional<Long> findLastSessionIdByUserId(@Param("userId") Long userId);


    boolean existsById(Long sessionId);

    @Query("SELECT us.sessionId FROM UserSession us WHERE us.userId = :userId AND us.sessionStatus = 1 ORDER BY us.loginTime DESC")
    Optional<Long> findLastActiveSessionIdByUserId(@Param("userId") Long userId);
    
    
    @Query("SELECT us FROM UserSession us WHERE us.userId = :userId AND us.sessionStatus = 1")
    Optional<UserSession> findActiveSessionByUserId(@Param("userId") Long userId);
    
    @Query("SELECT us FROM UserSession us WHERE us.userId = :userId AND us.sessionStatus = 1 ORDER BY us.loginTime DESC")
    Optional<UserSession> findLastActiveSessionByUserId(@Param("userId") Long userId);
}
