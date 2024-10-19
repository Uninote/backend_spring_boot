package com.uninote.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uninote.backend.service.UserFilterSearchService;
import com.uninote.backend.service.UserSessionService;

@RestController
@RequestMapping("/search-track")
public class UserFilterSearchController {

    @Autowired
    private UserFilterSearchService userFilterSearchService;

    @Autowired
    private UserSessionService userSessionService;
    

    @PostMapping("/add")
    public void trackSearch(
                         @RequestParam("userId") Long userId,
            @RequestParam(value = "universityId", required = false) Long universityId,
            @RequestParam(value = "departmentId", required = false) Long departmentId,
            @RequestParam(value = "semester", required = false) Integer semester,
            @RequestParam(value = "courseId", required = false) Long courseId
            ) {
                Long sessionId  = userSessionService.findLastSessionForUser(userId);
                userFilterSearchService.logSearch(userId, universityId, departmentId, semester, courseId, sessionId);   
            }
                        
            
}
