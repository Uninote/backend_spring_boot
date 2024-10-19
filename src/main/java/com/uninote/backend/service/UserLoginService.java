package com.uninote.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uninote.backend.repository.UserLoginRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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


    public Map<Integer, Long> getUserDistinctLoginDays(LocalDateTime startDate, LocalDateTime endDate) {
        
        List<Object[]> userDistinctLoginDays = userLoginRepository.findUserDistinctLoginDaysBetweenDates(startDate, endDate);

        
        Map<Integer, Long> loginDayCountMap = userDistinctLoginDays.stream()
            .collect(Collectors.groupingBy(
                row -> ((Number) row[1]).intValue(),  
                Collectors.counting()                 
            ));

        return loginDayCountMap;
    }
    public List<Map<String, Object>> getDistinctLoginsPerDay() {
        List<Object[]> results = userLoginRepository.findDistinctLoginsPerDay();

        return results.stream().map(result -> {
            Map<String, Object> map = new HashMap<>();
            map.put("login_day", result[0].toString());  
            map.put("distinct_logins", result[1]);        
            return map;
        }).collect(Collectors.toList());
    }
}
