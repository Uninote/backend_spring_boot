package com.uninote.backend.controller;

import com.uninote.backend.entity.University;
import com.uninote.backend.service.UniversityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/universities")
public class UniversityController {

    @Autowired
    private UniversityService universityService;

    @PostMapping("/add")
    public ResponseEntity<University> addUniversity(@RequestBody University university) {
        University savedUniversity = universityService.saveUniversity(university);
        return ResponseEntity.ok(savedUniversity);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<University> updateUniversity(@PathVariable Long id, @RequestBody University universityDetails) {
        Optional<University> university = universityService.findUniversityById(id);
        if (university.isPresent()) {
            University updatedUniversity = university.get();
            updatedUniversity.setLocation(universityDetails.getLocation());
            // Update other fields as necessary

            universityService.saveUniversity(updatedUniversity);
            return ResponseEntity.ok(updatedUniversity);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<University> getUniversityById(@PathVariable Long id) {
        Optional<University> university = universityService.findUniversityById(id);
        return university.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUniversity(@PathVariable Long id) {
        Optional<University> university = universityService.findUniversityById(id);
        if (university.isPresent()) {
            universityService.deleteUniversityById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}