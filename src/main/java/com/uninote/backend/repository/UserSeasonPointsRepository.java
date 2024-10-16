package com.uninote.backend.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import com.uninote.backend.entity.Season;
import com.uninote.backend.entity.User;
import com.uninote.backend.entity.UserSeasonPoints;

import java.util.List;
import java.util.Optional;

public interface UserSeasonPointsRepository extends JpaRepository<UserSeasonPoints, Long> {
    List<UserSeasonPoints> findBySeasonSeasonId(Long seasonId);
    List<UserSeasonPoints> findByUserId(Long userId);
    Optional<UserSeasonPoints> findByUserAndSeason(User user, Season season);

}
