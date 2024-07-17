package com.uninote.backend.service;

import com.uninote.backend.entity.UniversityName;
import com.uninote.backend.entity.Language;
import com.uninote.backend.repository.UniversityNameRepository;
import com.uninote.backend.repository.LanguageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UniversityNameService {

    @Autowired
    private UniversityNameRepository universityNameRepository;

    @Autowired
    private LanguageRepository languageRepository;

    public UniversityName saveUniversityName(UniversityName universityName) {
        return universityNameRepository.save(universityName);
    }

    public Optional<UniversityName> findUniversityNameById(Long id) {
        return universityNameRepository.findById(id);
    }

    public void deleteUniversityNameById(Long id) {
        universityNameRepository.deleteById(id);
    }

    public Optional<Language> findLanguageById(Long id) {
        return languageRepository.findById(id);
    }

    public Optional<Language> findLanguageByCode(String code) {
        return languageRepository.findByCode(code);
    }
}