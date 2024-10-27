package com.uninote.backend.controller;



import com.uninote.backend.entity.Season;
import com.uninote.backend.service.SeasonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@RestController
@RequestMapping("/seasons")
public class SeasonController {

    @Autowired
    private SeasonService seasonService;

    @GetMapping("/current")
    public ResponseEntity<Season> getCurrentSeason() {
        Optional<Season> currentSeason = seasonService.getCurrentSeason();
        return currentSeason.map(ResponseEntity::ok)
                            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/season-exists")
    public ResponseEntity<Boolean> existsCurrentSeason() {
        Optional<Season> currentSeason = seasonService.getCurrentSeason();
        return currentSeason.map(season -> ResponseEntity.ok(true))
                            .orElseGet(() -> ResponseEntity.ok(false));
    }


    @PostMapping("/create-season")
    public ResponseEntity<?> createNewSeason(
            @RequestParam("name") String name,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        
        LocalDateTime start;
        LocalDateTime end;

        try {
            start = LocalDateTime.parse(startDate);
            end = LocalDateTime.parse(endDate);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Invalid date format. Please use 'yyyy-MM-ddTHH:mm:ss'.");
        }

        if (start.isAfter(end)) {
            return ResponseEntity.badRequest().body("Start date must be before end date.");
        }

        Optional<Season> overlappingSeason = seasonService.findOverlappingSeason(start, end);
        if (overlappingSeason.isPresent()) {
            return ResponseEntity.badRequest().body("Season overlaps with existing season: " + overlappingSeason.get().getName());
        }

        Season season = new Season();
        season.setName(name);
        season.setStartDate(start);
        season.setEndDate(end);

        Season createdSeason = seasonService.createSeason(season);

        return ResponseEntity.ok("Season created successfully: " + createdSeason.getName());
    }

}
