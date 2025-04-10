package com.uninote.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uninote.backend.entity.CourseSimilarity;

public interface CourseSimilarityRepository extends JpaRepository<CourseSimilarity, Long> {
    List<CourseSimilarity> findByCourseA_IdOrderBySimilarityScoreDesc(Long courseId);
}
