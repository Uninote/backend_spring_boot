package com.uninote.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uninote.backend.entity.QuestionReport;

public interface QuestionReportRepository extends JpaRepository<QuestionReport, Long> {

    @Query("SELECT qr FROM QuestionReport qr WHERE qr.id.questionId = :questionId")
    List<QuestionReport> findByQuestionId(@Param("questionId") Long questionId);

} 
