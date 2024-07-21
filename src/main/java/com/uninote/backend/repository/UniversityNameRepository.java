package com.uninote.backend.repository;

import com.uninote.backend.entity.UniversityName;
import com.uninote.backend.entity.UniversityNameId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UniversityNameRepository extends JpaRepository<UniversityName, UniversityNameId> {
    
    List<UniversityName> findByUniversityId(Long universityId);

    
    List<UniversityName> findByLanguageId(Long languageId);

    
    Optional<UniversityName> findByUniversityIdAndLanguageId(Long universityId, Long languageId);

    
    List<UniversityName> findByNameContaining(String name);
}
