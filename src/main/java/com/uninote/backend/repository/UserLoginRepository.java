package com.uninote.backend.repository;

import com.uninote.backend.entity.UserLogin;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserLoginRepository extends JpaRepository<UserLogin, Long> {
    @Query("SELECT COUNT(DISTINCT u.user.id) FROM UserLogin u WHERE u.loginTimestamp >= :start AND u.loginTimestamp < :end")
    Long countDistinctUsersByLoginTimestampBetween(LocalDateTime start, LocalDateTime end);

    // Find users who have logged in on two consecutive days
    @Query("SELECT u.user.id FROM UserLogin u WHERE u.loginTimestamp >= :start AND u.loginTimestamp < :end GROUP BY u.user.id HAVING COUNT(DISTINCT TRUNC(u.loginTimestamp)) = 2")
    List<Long> findUsersLoggedInConsecutively(LocalDateTime start, LocalDateTime end);


    @Query(value = "SELECT ul.user_id, COUNT(DISTINCT DATE_TRUNC('day', ul.login_timestamp)) " +
                   "FROM user_logins ul " +
                   "WHERE ul.login_timestamp BETWEEN :startDate AND :endDate " +
                   "GROUP BY ul.user_id", 
           nativeQuery = true)
    List<Object[]> findUserDistinctLoginDaysBetweenDates(LocalDateTime startDate, LocalDateTime endDate);

    @Query(value = "SELECT DATE_TRUNC('day', login_timestamp) AS login_day, COUNT(DISTINCT user_id) AS distinct_logins " +
                    "FROM user_logins WHERE login_timestamp >= CURRENT_DATE - INTERVAL '30 days' " +
                    "GROUP BY DATE_TRUNC('day', login_timestamp) " +
                    "ORDER BY login_day", nativeQuery = true)
    List<Object[]> findDistinctLoginsPerDay();

}   