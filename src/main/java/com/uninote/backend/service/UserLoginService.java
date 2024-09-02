package com.uninote.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uninote.backend.repository.UserLoginRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class UserLoginService {

    @Autowired
    private UserLoginRepository userLoginRepository;

    public Long getDistinctUsersToday() {
        LocalDateTime startOfDay = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return userLoginRepository.countDistinctUsersByLoginTimestampBetween(startOfDay, endOfDay);
    }

    public List<Long> getUsersLoggedInConsecutively() {
        LocalDateTime startOfYesterday = LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.DAYS);
        LocalDateTime endOfToday = startOfYesterday.plusDays(2);
        return userLoginRepository.findUsersLoggedInConsecutively(startOfYesterday, endOfToday);
    }


    public Long getTotalDistinctUsers(LocalDateTime start, LocalDateTime end) {
        return userLoginRepository.countDistinctUsersByLoginTimestampBetween(start, end);
    }

    public List<Long> getUsersLoggedInConsecutively(LocalDateTime start, LocalDateTime end) {
        return userLoginRepository.findUsersLoggedInConsecutively(start, end);
    }
}
