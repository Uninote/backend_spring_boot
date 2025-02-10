package com.uninote.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uninote.backend.service.QrViewService;
import com.uninote.backend.service.UserService;

@RestController
@RequestMapping("/dashboard-metrics")
public class DashboardMetricsController {

    @Autowired
    private UserService userService;

    @Autowired
    private QrViewService qrViewService;

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
}
