package com.uninote.backend.repository;

import java.util.Optional;

import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uninote.backend.entity.UserSession;

@Repository
public interface UserSessionRepository  extends JpaRepository<UserSession, Long>{

    @Query(value ="select count(*) from users where email_verified = 1", nativeQuery = true)
    Long countTotalVerifiedUsers();

    @Query(value = "SELECT session_id FROM user_session WHERE user_id = :userId ORDER BY login_time DESC LIMIT 1", nativeQuery = true)
    Optional<Long> findLastSessionIdByUserId(@Param("userId") Long userId);


    boolean existsById(Long sessionId);

    @Query("SELECT us.sessionId FROM UserSession us WHERE us.userId = :userId AND us.sessionStatus = 1 ORDER BY us.loginTime DESC")
    Optional<Long> findLastActiveSessionIdByUserId(@Param("userId") Long userId);
    
    
    @Query("SELECT us FROM UserSession us WHERE us.userId = :userId AND us.sessionStatus = 1")
    Optional<UserSession> findActiveSessionByUserId(@Param("userId") Long userId);
    
    @Query(value = "SELECT * FROM (SELECT us.* FROM admin.user_sessions us WHERE us.user_id = :userId AND us.session_status = 1 ORDER BY us.login_time DESC) WHERE ROWNUM = 1", nativeQuery = true)
    Optional<UserSession> findLastActiveSessionByUserId(@Param("userId") Long userId);

}
