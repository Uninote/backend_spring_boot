package com.uninote.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uninote.backend.entity.Questionnaire;
import com.uninote.backend.entity.QuestionnaireStatus;

public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Long> {
    
    // Find questionnaires by status
    List<Questionnaire> findByStatus(QuestionnaireStatus status);
    
    // Find questionnaires by status and trigger time
    List<Questionnaire> findByStatusAndTriggerTimeBefore(QuestionnaireStatus status, LocalDateTime triggerTime);
    
    // Find questionnaires by Firebase path
    Optional<Questionnaire> findByFirebasePath(String firebasePath);
    
    // Find active questionnaires ready to trigger
    @Query("SELECT q FROM Questionnaire q WHERE q.status = :status AND q.triggerTime <= :now")
    List<Questionnaire> findReadyToTrigger(@Param("status") QuestionnaireStatus status, @Param("now") LocalDateTime now);
} 