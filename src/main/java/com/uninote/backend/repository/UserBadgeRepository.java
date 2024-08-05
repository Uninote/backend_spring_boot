package com.uninote.backend.repository;

import java.util.Optional;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uninote.backend.entity.Badge;
import com.uninote.backend.entity.User;
import com.uninote.backend.entity.UserBadge;


import com.uninote.backend.entity.UserBadgeId;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, UserBadgeId> {
    Optional<UserBadge> findById(UserBadgeId id);
    List<UserBadge> findByUser(User user);

    @Query("SELECT ub.badge FROM UserBadge ub WHERE ub.user.id = :userId")
    List<Badge> findBadgesByUserId(@Param("userId") Long userId);
}
