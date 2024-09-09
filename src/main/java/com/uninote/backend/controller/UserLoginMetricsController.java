package com.uninote.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uninote.backend.service.UserLoginService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/metrics")
public class UserLoginMetricsController {

    @Autowired
    private UserLoginService userLoginService;

    @GetMapping("/distinct-users-today")
    public ResponseEntity<Long> getDistinctUsersToday() {
        return ResponseEntity.ok(userLoginService.getDistinctUsersToday());
    }

    @GetMapping("/consecutive-logins")
    public ResponseEntity<List<Long>> getUsersLoggedInConsecutively() {
        return ResponseEntity.ok(userLoginService.getUsersLoggedInConsecutively());
    }

    @GetMapping("/distinct-users-by-time")
    public ResponseEntity<Long> getDistinctUsers(
            @RequestParam("start") String start,
            @RequestParam("end") String end) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime startDate = LocalDateTime.parse(start, formatter);
        LocalDateTime endDate = LocalDateTime.parse(end, formatter);

        Long count = userLoginService.getTotalDistinctUsers(startDate, endDate);
        return ResponseEntity.ok(count);
    }

    /**
     * Get the list of user IDs who have logged in consecutively today and yesterday.
     */
    @GetMapping("/consecutive-logins-by-time")
    public ResponseEntity<List<Long>> getUsersLoggedInConsecutively(
            @RequestParam("start") String start,
            @RequestParam("end") String end) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime startDate = LocalDateTime.parse(start, formatter);
        LocalDateTime endDate = LocalDateTime.parse(end, formatter);

        List<Long> userIds = userLoginService.getUsersLoggedInConsecutively(startDate, endDate);
        return ResponseEntity.ok(userIds);
    }
}
