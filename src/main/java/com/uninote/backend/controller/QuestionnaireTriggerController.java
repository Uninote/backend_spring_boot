package com.uninote.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uninote.backend.entity.Questionnaire;
import com.uninote.backend.service.QuestionnaireTriggerService;

@RestController
@RequestMapping("/api/questionnaire-trigger")
public class QuestionnaireTriggerController {

    @Autowired
    private QuestionnaireTriggerService questionnaireTriggerService;

   
    @PostMapping("/send/{questionnaireId}")
    public ResponseEntity<Map<String, Object>> sendQuestionnaireToEligibleUsers(
            @PathVariable Long questionnaireId) {
        try {
            questionnaireTriggerService.sendQuestionnaireToEligibleUsers(questionnaireId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Questionnaire sent to eligible users",
                "questionnaireId", questionnaireId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to send questionnaire: " + e.getMessage(),
                "questionnaireId", questionnaireId
            ));
        }
    }

   
    @PostMapping("/send-all")
    public ResponseEntity<Map<String, Object>> sendAllActiveQuestionnaires() {
        try {
            questionnaireTriggerService.triggerAllActiveQuestionnaires();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "All active questionnaires sent to eligible users"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to send questionnaires: " + e.getMessage()
            ));
        }
    }

  
    @GetMapping("/user/{userId}/questionnaire/{questionnaireId}/answered")
    public ResponseEntity<Map<String, Object>> hasUserAnsweredQuestionnaire(
            @PathVariable Long userId,
            @PathVariable Long questionnaireId) {
        
        boolean hasAnswered = questionnaireTriggerService.hasUserAnsweredQuestionnaire(userId, questionnaireId);
        
        return ResponseEntity.ok(Map.of(
            "userId", userId,
            "questionnaireId", questionnaireId,
            "hasAnswered", hasAnswered
        ));
    }

   
    @GetMapping("/user/{userId}/eligible-questionnaires")
    public ResponseEntity<Map<String, Object>> getEligibleQuestionnairesForUser(
            @PathVariable Long userId) {
        
        List<Questionnaire> eligibleQuestionnaires = questionnaireTriggerService.getEligibleQuestionnairesForUser(userId);
        
        return ResponseEntity.ok(Map.of(
            "userId", userId,
            "eligibleQuestionnaires", eligibleQuestionnaires,
            "count", eligibleQuestionnaires.size()
        ));
    }

    @GetMapping("/user/{userId}/questionnaire/{questionnaireId}/meets-criteria")
    public ResponseEntity<Map<String, Object>> checkUserMeetsCriteria(
            @PathVariable Long userId,
            @PathVariable Long questionnaireId) {
        
        // This would require injecting UserRepository and QuestionnaireRepository
        // For now, returning a placeholder response
        return ResponseEntity.ok(Map.of(
            "userId", userId,
            "questionnaireId", questionnaireId,
            "meetsCriteria", true, // Placeholder
            "message", "Criteria check functionality to be implemented"
        ));
    }
} 