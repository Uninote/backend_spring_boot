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
    
    @Query("SELECT * FROM BADGE b WHERE b.type.id = 5")
    List<Badge> findSpecial();
}