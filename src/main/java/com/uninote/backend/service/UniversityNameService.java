package com.uninote.backend.service;

import com.uninote.backend.converter.EntityToDTOConverter;
import com.uninote.backend.dto.UniversityNameDTO;
import com.uninote.backend.entity.Language;
import com.uninote.backend.entity.University;
import com.uninote.backend.entity.UniversityName;
import com.uninote.backend.entity.UniversityNameId;
import com.uninote.backend.repository.LanguageRepository;
import com.uninote.backend.repository.UniversityNameRepository;
import com.uninote.backend.repository.UniversityRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class UniversityNameService {

    @Autowired
    private UniversityNameRepository universityNameRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private LanguageRepository languageRepository; 

    public List<UniversityNameDTO> getAllUniversityNames() {
        return universityNameRepository.findAll().stream()
                .map(EntityToDTOConverter::convertUniversityNameToDTO)
                .collect(Collectors.toList());
    }

    public UniversityNameDTO getUniversityNameById(UniversityNameId id) {
        UniversityName universityName = universityNameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("UniversityName not found with id: " + id));
        return EntityToDTOConverter.convertUniversityNameToDTO(universityName);
    }

    @Transactional
    public University addUniversityName(UniversityNameDTO universityNameDTO) {
        Long universityId = universityNameDTO.getUniversityId();
        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new IllegalArgumentException("University not found"));

        // Retrieve the language entity
        Language language = languageRepository.findById(universityNameDTO.getLanguageId())
                .orElseThrow(() -> new IllegalArgumentException("Language not found"));

        // Create a new UniversityName entity
        UniversityName universityName = new UniversityName();
        universityName.setUniversity(university);
        universityName.setLanguage(language);
        universityName.setName(universityNameDTO.getName());
        universityName.setFullName(universityNameDTO.getFullName());

        // Create and set the composite key
        UniversityNameId universityNameId = new UniversityNameId(university.getId(), language.getId());
        universityName.setId(universityNameId);

        // Save the new UniversityName entity
        universityNameRepository.save(universityName);

        // Add the new name to the university's set of names
        university.getUniversityNames().add(universityName);

        // Save the updated university
        return universityRepository.saveAndFlush(university);
    }

    public UniversityNameDTO updateUniversityName(UniversityNameId id, UniversityNameDTO universityNameDTO) {
        UniversityName universityName = universityNameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("UniversityName not found with id: " + id));
        universityName.setName(universityNameDTO.getName());
        universityName.setFullName(universityNameDTO.getFullName());
        universityName = universityNameRepository.save(universityName);
        return EntityToDTOConverter.convertUniversityNameToDTO(universityName);
    }

    public void deleteUniversityName(UniversityNameId id) {
        universityNameRepository.deleteById(id);
    }

    
}
