package com.uninote.backend.service;

import com.uninote.backend.dto.UniversityDTO;
import com.uninote.backend.dto.UniversityNameDTO;
import com.uninote.backend.entity.University;
import com.uninote.backend.entity.UniversityName;
import com.uninote.backend.repository.UniversityRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class UniversityService {
    private static final Logger logger = LoggerFactory.getLogger(UniversityService.class);

    @Autowired
    private UniversityRepository universityRepository;

    public UniversityDTO getUniversityById(Long id) {
        University university = universityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("University not found"));
        logger.info("University found: {}", university);
        logger.info("University names: {}", university.getUniversityNames());
        return convertToDTO(university);
    }
    private UniversityDTO convertToDTO(University university) {
        return new UniversityDTO(
            university.getId(),
            university.getLocation(),
            university.getUniversityNames().stream().map(this::convertNameToDTO).collect(Collectors.toSet())
        );
    }

    private UniversityNameDTO convertNameToDTO(UniversityName universityName) {
        return new UniversityNameDTO(
            universityName.getUniversity().getId().toString(),
            universityName.getLanguage().getId().toString(),
            universityName.getName(),
            universityName.getFullName()
        );
    }
}
