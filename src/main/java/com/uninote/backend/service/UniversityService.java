package com.uninote.backend.service;

import com.uninote.backend.entity.University;
import com.uninote.backend.repository.UniversityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UniversityService {

    @Autowired
    private UniversityRepository universityRepository;

    public University saveUniversity(University university) {
        return universityRepository.save(university);
    }

    public Optional<University> findUniversityById(Long id) {
        return universityRepository.findById(id);
    }

    public void deleteUniversityById(Long id) {
        universityRepository.deleteById(id);
    }
}