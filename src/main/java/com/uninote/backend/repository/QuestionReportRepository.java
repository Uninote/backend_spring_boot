package com.uninote.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uninote.backend.entity.QuestionReport;

public interface QuestionReportRepository extends JpaRepository<QuestionReport, Long> {
}
