package com.uninote.backend.service;

import com.uninote.backend.dto.UniversityNameDTO;
import com.uninote.backend.entity.UniversityName;
import com.uninote.backend.entity.UniversityNameId;
import com.uninote.backend.repository.UniversityNameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UniversityNameService {

    @Autowired
    private UniversityNameRepository universityNameRepository;

    public List<UniversityNameDTO> getAllUniversityNames() {
        return universityNameRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UniversityNameDTO getUniversityNameById(UniversityNameId id) {
        UniversityName universityName = universityNameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("UniversityName not found with id: " + id));
        return convertToDTO(universityName);
    }

    public UniversityNameDTO createUniversityName(UniversityNameDTO universityNameDTO) {
        UniversityName universityName = new UniversityName(
                new UniversityNameId(
                        Long.parseLong(universityNameDTO.getUniversityId()),
                        Long.parseLong(universityNameDTO.getLanguageId())
                ),
                null, 
                null,
                universityNameDTO.getName(),
                universityNameDTO.getFullName()
        );
        universityName = universityNameRepository.save(universityName);
        return convertToDTO(universityName);
    }

    public UniversityNameDTO updateUniversityName(UniversityNameId id, UniversityNameDTO universityNameDTO) {
        UniversityName universityName = universityNameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("UniversityName not found with id: " + id));
        universityName.setName(universityNameDTO.getName());
        universityName.setFullName(universityNameDTO.getFullName());
        universityName = universityNameRepository.save(universityName);
        return convertToDTO(universityName);
    }

    public void deleteUniversityName(UniversityNameId id) {
        universityNameRepository.deleteById(id);
    }

    private UniversityNameDTO convertToDTO(UniversityName universityName) {
        return new UniversityNameDTO(
                universityName.getUniversity().getId().toString(),
                universityName.getLanguage().getId().toString(),
                universityName.getName(),
                universityName.getFullName()
        );
    }
}
