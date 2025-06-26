package com.uninote.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uninote.backend.entity.Questionnaire;
import com.uninote.backend.entity.QuestionnaireResponse;
import com.uninote.backend.entity.User;

public interface QuestionnaireResponseRepository extends JpaRepository<QuestionnaireResponse, Long> {
    
    // Check if user has already answered a specific questionnaire
    boolean existsByQuestionnaireAndUser(Questionnaire questionnaire, User user);
    
    // Check if user has already answered a specific questionnaire (by IDs)
    boolean existsByQuestionnaire_IdAndUser_Id(Long questionnaireId, Long userId);
    
    // Get all responses for a specific questionnaire
    List<QuestionnaireResponse> findByQuestionnaire(Questionnaire questionnaire);
    
    // Get all responses for a specific user
    List<QuestionnaireResponse> findByUser(User user);
    
    // Get response by questionnaire and user
    Optional<QuestionnaireResponse> findByQuestionnaireAndUser(Questionnaire questionnaire, User user);
    
    // Get response by questionnaire ID and user ID
    Optional<QuestionnaireResponse> findByQuestionnaire_IdAndUser_Id(Long questionnaireId, Long userId);
    
    // Get all users who have answered a specific questionnaire
    @Query("SELECT qr.user FROM QuestionnaireResponse qr WHERE qr.questionnaire.id = :questionnaireId")
    List<User> findUsersWhoAnsweredQuestionnaire(@Param("questionnaireId") Long questionnaireId);
    
    // Get all users who have NOT answered a specific questionnaire
    @Query("SELECT u FROM User u WHERE u.id NOT IN " +
           "(SELECT qr.user.id FROM QuestionnaireResponse qr WHERE qr.questionnaire.id = :questionnaireId)")
    List<User> findUsersWhoHaventAnsweredQuestionnaire(@Param("questionnaireId") Long questionnaireId);
    
    // Count responses for a questionnaire
    long countByQuestionnaire(Questionnaire questionnaire);
    
    // Count responses for a questionnaire by ID
    long countByQuestionnaire_Id(Long questionnaireId);
    
    // Get responses by response ID
    Optional<QuestionnaireResponse> findByResponseId(String responseId);
    
    // Get incomplete responses for a user
    List<QuestionnaireResponse> findByUserAndIsCompleteFalse(User user);
    
    // Get complete responses for a user
    List<QuestionnaireResponse> findByUserAndIsCompleteTrue(User user);
} 