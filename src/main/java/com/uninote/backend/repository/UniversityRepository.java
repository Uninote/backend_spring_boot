package com.uninote.backend.repository;

import com.uninote.backend.entity.University;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UniversityRepository extends JpaRepository<University, Long> {
    Optional<University> findById(Long id);
    List<University> findByLocation(String location);
}