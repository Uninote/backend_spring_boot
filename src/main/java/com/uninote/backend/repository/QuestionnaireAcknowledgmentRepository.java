package com.uninote.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uninote.backend.entity.Questionnaire;
import com.uninote.backend.entity.QuestionnaireAcknowledgment;
import com.uninote.backend.entity.User;

public interface QuestionnaireAcknowledgmentRepository extends JpaRepository<QuestionnaireAcknowledgment, Long> {
    
    // Find acknowledgments by questionnaire and user
    List<QuestionnaireAcknowledgment> findByQuestionnaireAndUser(Questionnaire questionnaire, User user);
    
    // Find acknowledgments by questionnaire ID and user ID
    List<QuestionnaireAcknowledgment> findByQuestionnaire_IdAndUser_Id(Long questionnaireId, Long userId);
    
    // Find acknowledgments by user
    List<QuestionnaireAcknowledgment> findByUser(User user);
    
    // Find acknowledgments by questionnaire
    List<QuestionnaireAcknowledgment> findByQuestionnaire(Questionnaire questionnaire);
    
    // Find acknowledgments by type
    List<QuestionnaireAcknowledgment> findByAcknowledgmentType(String acknowledgmentType);
    
    // Find acknowledgments by user and type
    List<QuestionnaireAcknowledgment> findByUserAndAcknowledgmentType(User user, String acknowledgmentType);
    
    // Find acknowledgments by questionnaire and type
    List<QuestionnaireAcknowledgment> findByQuestionnaireAndAcknowledgmentType(Questionnaire questionnaire, String acknowledgmentType);
    
    // Find latest acknowledgment for a user and questionnaire
    Optional<QuestionnaireAcknowledgment> findFirstByQuestionnaireAndUserOrderByAcknowledgedAtDesc(Questionnaire questionnaire, User user);
    
    // Find latest acknowledgment by questionnaire ID and user ID
    Optional<QuestionnaireAcknowledgment> findFirstByQuestionnaire_IdAndUser_IdOrderByAcknowledgedAtDesc(Long questionnaireId, Long userId);
    
    // Find acknowledgments within a date range
    List<QuestionnaireAcknowledgment> findByAcknowledgedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find acknowledgments by session ID
    List<QuestionnaireAcknowledgment> findBySessionId(String sessionId);
    
    // Count acknowledgments by questionnaire and type
    long countByQuestionnaireAndAcknowledgmentType(Questionnaire questionnaire, String acknowledgmentType);
    
    // Count acknowledgments by user and type
    long countByUserAndAcknowledgmentType(User user, String acknowledgmentType);
    
    // Check if user has acknowledged a questionnaire with specific type
    boolean existsByQuestionnaireAndUserAndAcknowledgmentType(Questionnaire questionnaire, User user, String acknowledgmentType);
    
    // Check if user has acknowledged a questionnaire with specific type (by IDs)
    boolean existsByQuestionnaire_IdAndUser_IdAndAcknowledgmentType(Long questionnaireId, Long userId, String acknowledgmentType);
    
    // Find all users who have acknowledged a specific questionnaire
    @Query("SELECT DISTINCT qa.user FROM QuestionnaireAcknowledgment qa WHERE qa.questionnaire.id = :questionnaireId")
    List<User> findUsersWhoAcknowledgedQuestionnaire(@Param("questionnaireId") Long questionnaireId);
    
    // Find all users who have acknowledged a specific questionnaire with specific type
    @Query("SELECT DISTINCT qa.user FROM QuestionnaireAcknowledgment qa WHERE qa.questionnaire.id = :questionnaireId AND qa.acknowledgmentType = :acknowledgmentType")
    List<User> findUsersWhoAcknowledgedQuestionnaireWithType(@Param("questionnaireId") Long questionnaireId, @Param("acknowledgmentType") String acknowledgmentType);
    
    // Get acknowledgment statistics for a questionnaire
    @Query("SELECT qa.acknowledgmentType, COUNT(qa) FROM QuestionnaireAcknowledgment qa WHERE qa.questionnaire.id = :questionnaireId GROUP BY qa.acknowledgmentType")
    List<Object[]> getAcknowledgmentStatisticsForQuestionnaire(@Param("questionnaireId") Long questionnaireId);
    
    // Get acknowledgment statistics for a user
    @Query("SELECT qa.acknowledgmentType, COUNT(qa) FROM QuestionnaireAcknowledgment qa WHERE qa.user.id = :userId GROUP BY qa.acknowledgmentType")
    List<Object[]> getAcknowledgmentStatisticsForUser(@Param("userId") Long userId);
    
    // Find acknowledgments by questionnaire, user, and date range
    List<QuestionnaireAcknowledgment> findByQuestionnaireAndUserAndAcknowledgedAtBetween(
        Questionnaire questionnaire, User user, LocalDateTime startDate, LocalDateTime endDate);
    
    // Find acknowledgments by questionnaire ID, user ID, and date range
    List<QuestionnaireAcknowledgment> findByQuestionnaire_IdAndUser_IdAndAcknowledgedAtBetween(
        Long questionnaireId, Long userId, LocalDateTime startDate, LocalDateTime endDate);
} 