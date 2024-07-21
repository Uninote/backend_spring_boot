package com.uninote.backend.controller;

import com.uninote.backend.dto.UniversityNameDTO;
import com.uninote.backend.entity.UniversityNameId;
import com.uninote.backend.service.UniversityNameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/university-names")
public class UniversityNameController {

    @Autowired
    private UniversityNameService universityNameService;

    @GetMapping
    public List<UniversityNameDTO> getAllUniversityNames() {
        return universityNameService.getAllUniversityNames();
    }

    @GetMapping("/{universityId}/{languageId}")
    public ResponseEntity<UniversityNameDTO> getUniversityNameById(@PathVariable Long universityId, @PathVariable Long languageId) {
        UniversityNameId id = new UniversityNameId(universityId, languageId);
        return ResponseEntity.ok(universityNameService.getUniversityNameById(id));
    }

    @PostMapping
    public ResponseEntity<UniversityNameDTO> createUniversityName(@RequestBody UniversityNameDTO universityNameDTO) {
        return ResponseEntity.ok(universityNameService.createUniversityName(universityNameDTO));
    }

    @PutMapping("/{universityId}/{languageId}")
    public ResponseEntity<UniversityNameDTO> updateUniversityName(@PathVariable Long universityId, @PathVariable Long languageId, @RequestBody UniversityNameDTO universityNameDTO) {
        UniversityNameId id = new UniversityNameId(universityId, languageId);
        return ResponseEntity.ok(universityNameService.updateUniversityName(id, universityNameDTO));
    }

    @DeleteMapping("/{universityId}/{languageId}")
    public ResponseEntity<Void> deleteUniversityName(@PathVariable Long universityId, @PathVariable Long languageId) {
        UniversityNameId id = new UniversityNameId(universityId, languageId);
        universityNameService.deleteUniversityName(id);
        return ResponseEntity.noContent().build();
    }
}
