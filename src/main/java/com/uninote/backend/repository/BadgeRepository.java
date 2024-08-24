package com.uninote.backend.repository;

import com.uninote.backend.entity.Badge;
import com.uninote.backend.interfaceProjection.BadgeProjection;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {

    @Query("SELECT b.id AS id, ub.user.id AS userId, b.name AS name, b.description AS description, b.imageUrl AS imageUrl, " +
           "b.requirement AS requirement, bt.name AS typeName, CASE WHEN ub IS NOT NULL THEN true ELSE false END AS userHasBadge " +
           "FROM Badge b " +
           "LEFT JOIN UserBadge ub ON b.id = ub.badge.id AND ub.user.id = :userId " +
           "JOIN BadgeType bt ON b.type.id = bt.id")
    List<BadgeProjection> findAllBadgesByUserId(@Param("userId") Long userId);
    
}