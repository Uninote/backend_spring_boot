package com.uninote.backend.controller;

import com.uninote.backend.dto.UniversityDTO;
import com.uninote.backend.service.UniversityService;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/universities")
public class UniversityController {

    @Autowired
    private UniversityService universityService;

    @GetMapping("/{id}")
    public ResponseEntity<UniversityDTO> getUniversityById(@PathVariable Long id) {
        try {
            UniversityDTO university = universityService.getUniversityById(id);
            return ResponseEntity.ok(university);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) { 
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/details")
    public ResponseEntity<List<Map<String, String>>> getUniversityDetails(@RequestParam String language) {
        List<Map<String, String>> universityDetails = universityService.getUniversityDetails(language);
        return ResponseEntity.ok(universityDetails);
    }
}
