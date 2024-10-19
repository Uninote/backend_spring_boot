package com.uninote.backend.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uninote.backend.entity.Season;
import com.uninote.backend.entity.UserSeasonPoints;
import com.uninote.backend.interfaceProjection.UserInfoProjection;
import com.uninote.backend.repository.SeasonRepository;
import com.uninote.backend.repository.UserSeasonPointsRepository;


import java.util.List;
import java.util.Optional;

@Service
public class UserSeasonPointsService {

    @Autowired
    private UserSeasonPointsRepository userSeasonPointsRepository;

    @Autowired
    private SeasonService seasonService;

    public UserSeasonPoints addPoints(UserSeasonPoints userSeasonPoints) {
        return userSeasonPointsRepository.save(userSeasonPoints);
    }

    public List<UserSeasonPoints> getSeasonPoints(Long seasonId) {
        return userSeasonPointsRepository.findByIdSeasonId(seasonId);
    }

    public List<UserSeasonPoints> getUserPoints(Long userId) {
        return userSeasonPointsRepository.findByIdUserId(userId);
    }

    public List<UserInfoProjection> getTop100UsersByCurrentSeason() {
        Season season = seasonService.getCurrentSeason().orElseThrow(() -> new IllegalArgumentException("No current Season"));
        Long seasonId = season.getSeasonId();
        List<UserInfoProjection> res = userSeasonPointsRepository.top100UsersPerSeason(seasonId);
        return res;
    }

    public List<UserInfoProjection> getTop100UsersByCurrentSeasonAndUniversity(Long universityId) {
        Season season = seasonService.getCurrentSeason().orElseThrow(() -> new IllegalArgumentException("No current Season"));
        Long seasonId = season.getSeasonId();
        List<UserInfoProjection> res = userSeasonPointsRepository.top100UsersPerSeasonAndUniversity(seasonId,universityId);
        return res;
    }
}
