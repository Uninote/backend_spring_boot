package com.uninote.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uninote.backend.entity.UserDailyTokenQuota;

import java.sql.Date;
import java.util.Optional;

@Repository
public interface UserDailyTokenQuotaRepository extends JpaRepository<UserDailyTokenQuota, Long> {
    Optional<UserDailyTokenQuota> findByUserIdAndQuotaDate(Long userId, Date quotaDate);
}
