package com.uninote.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uninote.backend.dto.RankDTO;
import com.uninote.backend.entity.Rank;
import com.uninote.backend.service.RankService;

@RestController
@RequestMapping("/ranks")
public class RankController {

    @Autowired
    private RankService rankService;

    @GetMapping
    public ResponseEntity<List<RankDTO>> getRanks(){
        return ResponseEntity.ok(rankService.getRanks());
    } 
}
