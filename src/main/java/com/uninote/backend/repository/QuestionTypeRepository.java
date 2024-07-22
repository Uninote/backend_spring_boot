package com.uninote.backend.repository;

import com.uninote.backend.entity.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionTypeRepository extends JpaRepository<QuestionType, Long> {
}
