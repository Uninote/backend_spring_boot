package com.uninote.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uninote.backend.interfaceProjection.UserInfoProjection;
import com.uninote.backend.service.UserSeasonPointsService;

@RestController
@RequestMapping("/season-ranks")
public class UserSeasonPointsController {

    @Autowired
    private UserSeasonPointsService userSeasonPointsService;

    @GetMapping("/current-season")
    public ResponseEntity<List<UserInfoProjection>> getTop100UserCurrentSeason() {
        List<UserInfoProjection> users = userSeasonPointsService.getTop100UsersByCurrentSeason();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/current-season/university/{unive  rsityId}")
    public ResponseEntity<List<UserInfoProjection>> getTop100UserCurrentSeasonAndUniversity(@PathVariable Long universityId) {
        List<UserInfoProjection> users = userSeasonPointsService.getTop100UsersByCurrentSeasonAndUniversity(universityId);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/current-season/department/{departmentId}")
    public ResponseEntity<List<UserInfoProjection>> getTop100UserCurrentSeasonAndDepartment(@PathVariable Long departmentId) {
        List<UserInfoProjection> users = userSeasonPointsService.getTop100UsersByCurrentSeasonAndDepartment(departmentId);
        return ResponseEntity.ok(users);
    }
}
