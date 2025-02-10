package com.uninote.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uninote.backend.dto.GrowthStatisticsDTO;
import com.uninote.backend.dto.MonthlyActiveUsersDTO;
import com.uninote.backend.dto.NoteMetricDTO;
import com.uninote.backend.dto.UserGrowthDTO;
import com.uninote.backend.repository.UserRepository;
import com.uninote.backend.service.NoteMetricService;
import com.uninote.backend.service.QrViewService;
import com.uninote.backend.service.UserLoginService;
import com.uninote.backend.service.UserService;

@RestController
@RequestMapping("/dashboard-metrics")
public class DashboardMetricsController {

    @Autowired
    private UserService userService;

    @Autowired
    private QrViewService qrViewService;

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private NoteMetricService noteMetricService;

    @GetMapping("/total-users")
    public Long getTotalUsers() {
        return userService.getTotalUsers();
    }

    @GetMapping("/total-scans")
    public Long getTotalScans() {
        return qrViewService.countViews();
    }


    @GetMapping("/total-unverified-users")
    public Long getTotalUnverifiedUsers() {
        return userService.getTotalUnverifiedUsers();
    }

    @GetMapping("/deleted-users")
    public Long getDeletedUsers() {
        return userService.getDeletedUsers();
    }


    @GetMapping("/growth-over-time")
    public ResponseEntity<List<UserGrowthDTO>> getUserGrowthOverTime() {
        List<UserGrowthDTO> growthData = userService.fetchGrowthOverTime();
        return ResponseEntity.ok(growthData);
    }

    @GetMapping("/growth-statistics")
    public ResponseEntity<GrowthStatisticsDTO> getGrowthStatistics() {
        GrowthStatisticsDTO statistics = userService.fetchGrowthStatistics();
        return ResponseEntity.ok(statistics);
    }


    @GetMapping("/monthly-active-users")
    public List<MonthlyActiveUsersDTO> getMonthlyActiveUsers() {
        return userService.getMonthlyActiveUsers();
    }

    @GetMapping("/logins-last-30-days")
    public List<Map<String, Object>> getLoginsLast30Days() {
        return userLoginService.getDistinctLoginsPerDay();
    }

    @GetMapping("/notes-per-department")
    public List<NoteMetricDTO> getNoteMetrics() {
        return noteMetricService.getNoteMetrics();
    }
}
