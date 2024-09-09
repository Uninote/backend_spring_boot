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


    @Query(value = "SELECT ul.USER_ID, COUNT(DISTINCT TRUNC(ul.LOGIN_TIMESTAMP)) " +
                   "FROM USER_LOGINS ul " +
                   "WHERE ul.LOGIN_TIMESTAMP BETWEEN :startDate AND :endDate " +
                   "GROUP BY ul.USER_ID", 
           nativeQuery = true)
    List<Object[]> findUserDistinctLoginDaysBetweenDates(LocalDateTime startDate, LocalDateTime endDate);

}   