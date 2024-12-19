package com.uninote.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import com.uninote.backend.dto.NoteDTO;
import com.uninote.backend.service.SVDRecommendationService;

@RestController
@RequestMapping("/personalized-recommendations")
public class SVDRecommendationController {

    @Autowired
    private SVDRecommendationService svdRecommendationService;
    

    @GetMapping("/{userId}")
    public List<NoteDTO> getRecommendationsForUser(@PathVariable Long userId, @RequestParam(defaultValue = "EN") String language) {
        return svdRecommendationService.getRecommendationsForUser(userId, language);
    }
}
