package com.uninote.backend.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uninote.backend.entity.UserSeasonPoints;
import com.uninote.backend.repository.UserSeasonPointsRepository;

import java.util.List;

@Service
public class UserSeasonPointsService {

    @Autowired
    private UserSeasonPointsRepository userSeasonPointsRepository;

    public UserSeasonPoints addPoints(UserSeasonPoints userSeasonPoints) {
        return userSeasonPointsRepository.save(userSeasonPoints);
    }

    public List<UserSeasonPoints> getSeasonPoints(Long seasonId) {
        return userSeasonPointsRepository.findBySeasonSeasonId(seasonId);
    }

    public List<UserSeasonPoints> getUserPoints(Long userId) {
        return userSeasonPointsRepository.findByUserId(userId);
    }
}
