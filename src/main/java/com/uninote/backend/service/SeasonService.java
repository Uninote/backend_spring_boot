package com.uninote.backend.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uninote.backend.entity.Season;
import com.uninote.backend.entity.User;
import com.uninote.backend.entity.UserSeasonPoints;
import com.uninote.backend.entity.UserSeasonPointsId;
import com.uninote.backend.repository.SeasonRepository;
import com.uninote.backend.repository.UserRepository;
import com.uninote.backend.repository.UserSeasonPointsRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SeasonService {

    @Autowired
    private SeasonRepository seasonRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSeasonPointsRepository userSeasonPointsRepository;

    @Transactional
    public Season createSeason(Season season) {
        Season savedSeason = seasonRepository.save(season);
        List<User> allUsers = userRepository.findAllByUsernameIsNotNull();

        for (User user : allUsers) {
            UserSeasonPointsId userSeasonPointsId = new UserSeasonPointsId();
            userSeasonPointsId.setSeasonId(savedSeason.getSeasonId());
            userSeasonPointsId.setUserId(user.getId());
            UserSeasonPoints userSeasonPoints = new UserSeasonPoints(userSeasonPointsId, 0, null, false);
            user.setSeasonScore(0L);
            userRepository.save(user);
            userSeasonPointsRepository.save(userSeasonPoints); 
        }

        return savedSeason;
    }

    public Optional<Season> findOverlappingSeason(LocalDateTime startDate, LocalDateTime endDate) {
        return seasonRepository.findOverlappingSeason(startDate, endDate);
    }

    public List<Season> getAllSeasons() {
        return seasonRepository.findAll();
    }

    public Optional<Season> getSeasonById(Long seasonId) {
        return seasonRepository.findById(seasonId);
    }

    public void deleteSeason(Long seasonId) {
        seasonRepository.deleteById(seasonId);
    }

    public Optional<Season> getCurrentSeason() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        return seasonRepository.findCurrentSeason(currentDateTime);
    }
}
