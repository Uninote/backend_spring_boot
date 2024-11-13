package com.uninote.backend.controller;


import com.uninote.backend.dto.NoteDTO;
import com.uninote.backend.service.RecommendationService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recommended-notes")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @GetMapping
    public List<NoteDTO> getRecommendations() {
        return recommendationService.getCachedRecommendations();
    }


}
