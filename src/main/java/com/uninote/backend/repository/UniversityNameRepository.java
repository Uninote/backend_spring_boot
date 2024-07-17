package com.uninote.backend.repository;

import com.uninote.backend.entity.UniversityName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UniversityNameRepository extends JpaRepository<UniversityName, Long> {
    
}