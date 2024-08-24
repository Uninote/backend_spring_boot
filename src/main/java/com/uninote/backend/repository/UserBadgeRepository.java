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
import com.uninote.backend.interfaceProjection.BadgeProjection;

import org.springframework.stereotype.Repository;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, UserBadgeId> {
    Optional<UserBadge> findById(UserBadgeId id);
    List<UserBadge> findByUser(User user);

    @Query("SELECT ub.badge FROM UserBadge ub WHERE ub.user.id = :userId")
    List<Badge> findBadgesByUserId(@Param("userId") Long userId);

    @Query("SELECT b.id AS id, ub.user.id AS userId, b.name AS name, b.description AS description, b.imageUrl AS imageUrl, " +
           "b.requirement AS requirement, bt.name AS typeName, CASE WHEN ub IS NOT NULL THEN true ELSE false END AS userHasBadge " +
           "FROM Badge b " +
           "LEFT JOIN UserBadge ub ON b.id = ub.badge.id AND ub.user.id = :userId " +
           "JOIN BadgeType bt ON b.type.id = bt.id")
    List<BadgeProjection> findAllBadgesByUserId(@Param("userId") Long userId);
}
