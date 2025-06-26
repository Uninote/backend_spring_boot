package com.uninote.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uninote.backend.entity.QuestionnaireCriteria;

public interface QuestionnaireCriteriaRepository extends JpaRepository<QuestionnaireCriteria, Long> {
    List<QuestionnaireCriteria> findByQuestionnaireId(Long questionnaireId);
} 