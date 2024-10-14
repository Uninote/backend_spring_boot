package com.uninote.backend.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import com.uninote.backend.entity.UserSeasonPoints;

import java.util.List;

public interface UserSeasonPointsRepository extends JpaRepository<UserSeasonPoints, Long> {
    List<UserSeasonPoints> findBySeasonSeasonId(Long seasonId);
    List<UserSeasonPoints> findByUserId(Long userId);
}
