package com.uninote.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uninote.backend.entity.DepartmentSimilarity;

public interface DepartmentSimilarityRepository extends JpaRepository<DepartmentSimilarity, Long> {
    List<DepartmentSimilarity> findByDepartmentA_IdOrderBySimilarityScoreDesc(Long departmentId);
}

