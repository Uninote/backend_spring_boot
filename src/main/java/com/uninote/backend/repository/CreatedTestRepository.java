package com.uninote.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.uninote.backend.entity.CreatedTest;

import java.util.Date;
import java.util.List;

@Repository
public interface CreatedTestRepository extends JpaRepository<CreatedTest, Long> {

    
    @Query("SELECT COUNT(ct) FROM CreatedTest ct")
    Long countTotalTests();

    
    @Query("SELECT COUNT(ct) FROM CreatedTest ct WHERE ct.userId = :userId")
    Long countTestsByUser(Long userId);

    
    @Query("SELECT COUNT(ct) FROM CreatedTest ct WHERE ct.typeId = :typeId")
    Long countTestsByType(Long typeId);

    
    @Query("SELECT COUNT(ct) FROM CreatedTest ct WHERE ct.creationDate BETWEEN :start AND :end")
    Long countTestsByDateRange(Date start, Date end);

    @Query("SELECT ct.typeId FROM CreatedTest ct GROUP BY ct.typeId ORDER BY COUNT(ct) DESC")
    Long findMostCommonTestType();
}
