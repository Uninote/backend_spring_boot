package com.uninote.backend.service;

import com.uninote.backend.converter.EntityToDTOConverter;
import com.uninote.backend.dto.UniversityDTO;
import com.uninote.backend.dto.UniversityNameDTO;
import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.University;
import com.uninote.backend.entity.UniversityName;
import com.uninote.backend.interfaceProjection.UniversityDetailsProjection;
import com.uninote.backend.repository.UniversityRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UniversityService {
    private static final Logger logger = LoggerFactory.getLogger(UniversityService.class);

    @Autowired
    private UniversityRepository universityRepository;


    /*public List<Map<String, String>> getUniversityDetails(String language) {
        List<University> universities = universityRepository.findAll();
        List<UniversityDTO> universityDTOs = universities.stream()
                                                         .map(EntityToDTOConverter::convertUniversityToDTO)
                                                         .collect(Collectors.toList());
        List<Map<String, String>> result = new ArrayList<>();

        for (UniversityDTO university : universityDTOs) {
            for (UniversityNameDTO nameDTO : university.getUniversityNames()) {
                if (nameDTO.getLanguageId().equals(language)) {
                    Map<String, String> uniMap = new HashMap<>();
                    uniMap.put("id", String.valueOf(university.getId()));
                    uniMap.put("fullName", nameDTO.getFullName());
                    uniMap.put("name", nameDTO.getName());
                    result.add(uniMap);
                    break;
                }
            }
        }

        return result;
    }*/
    public List<Map<String, String>> getUniversityDetails(String language) {
        return universityRepository.findUniversityDetailsByLanguage(language).stream()
                .map(projection -> {
                    Map<String, String> uniMap = new HashMap<>();
                    uniMap.put("id", String.valueOf(projection.getId()));
                    uniMap.put("fullName", projection.getFullName());
                    uniMap.put("name", projection.getName());
                    return uniMap;
                })
                .collect(Collectors.toList());
    }
    


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
        university.getUniversityNames().stream().map(EntityToDTOConverter::convertUniversityNameToDTO).collect(Collectors.toSet()),
        university.getDepartments().stream().map(Department::getId).collect(Collectors.toSet())
    );
}
 public List<UniversityDetailsProjection> getUniversitiesWithQuestionsInLanguage(String language) {
        return universityRepository.findUniversitiesWithQuestionsInLanguage(language);
    }

    
}
