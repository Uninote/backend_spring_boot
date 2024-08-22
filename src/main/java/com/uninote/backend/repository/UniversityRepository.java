package com.uninote.backend.repository;

import com.uninote.backend.entity.University;
import com.uninote.backend.interfaceProjection.UniversityDetailsProjection;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UniversityRepository extends JpaRepository<University, Long> {
    Optional<University> findById(Long id);
    List<University> findByLocation(String location);

    @Query("SELECT n.university.id AS id, n.fullName AS fullName, n.name AS name " +
            "FROM UniversityName n JOIN n.language l " +
            "WHERE l.code = :language")
    List<UniversityDetailsProjection> findUniversityDetailsByLanguage(String language);
}