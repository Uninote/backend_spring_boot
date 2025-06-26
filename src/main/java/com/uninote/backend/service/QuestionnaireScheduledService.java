package com.uninote.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class QuestionnaireScheduledService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionnaireScheduledService.class);

    @Autowired
    private QuestionnaireTriggerService questionnaireTriggerService;

    @PostConstruct
    public void init() {
        logger.info("QuestionnaireScheduledService initialized - scheduled tasks will run every 30s, 1min, and 2min");
        logger.info("⚠️  FIREBASE PRESENCE CHECK IS TEMPORARILY DISABLED for testing");
    }

    @Scheduled(fixedRate = 30000)
    public void hourlyQuestionnaireCheck() {
        logger.info("SCHEDULED TASK: Starting hourly questionnaire check (30s interval)");
        try {
            logger.debug("Calling questionnaireTriggerService.triggerAllActiveQuestionnaires(false) - DISABLED PRESENCE CHECK");
            questionnaireTriggerService.triggerAllActiveQuestionnaires(false);
            logger.info("✅ SCHEDULED TASK: Successfully completed hourly questionnaire check");
        } catch (Exception e) {
            logger.error("❌ SCHEDULED TASK: Error in hourly questionnaire check: {}", e.getMessage(), e);
        }
        logger.debug("=== Completed hourly questionnaire check ===");
    }

    @Scheduled(cron = "0 */2 * * * ?") 
    public void dailyQuestionnaireCheck() {
        logger.info("🕐 SCHEDULED TASK: Starting daily questionnaire check (2min interval)");
        try {
            logger.debug("Calling questionnaireTriggerService.triggerAllActiveQuestionnaires(false) - DISABLED PRESENCE CHECK");
            questionnaireTriggerService.triggerAllActiveQuestionnaires(false);
            logger.info("✅ SCHEDULED TASK: Successfully completed daily questionnaire check");
        } catch (Exception e) {
            logger.error("❌ SCHEDULED TASK: Error in daily questionnaire check: {}", e.getMessage(), e);
        }
        logger.debug("=== Completed daily questionnaire check ===");
    }

    @Scheduled(fixedRate = 60000)
    public void sixHourlyQuestionnaireCheck() {
        logger.info("SCHEDULED TASK: Starting 6-hourly questionnaire check (1min interval)");
        try {
            logger.debug("Calling questionnaireTriggerService.triggerAllActiveQuestionnaires(false) - DISABLED PRESENCE CHECK");
            questionnaireTriggerService.triggerAllActiveQuestionnaires(false);
            logger.info("SCHEDULED TASK: Successfully completed 6-hourly questionnaire check");
        } catch (Exception e) {
            logger.error("SCHEDULED TASK: Error in 6-hourly questionnaire check: {}", e.getMessage(), e);
        }
        logger.debug("=== Completed 6-hourly questionnaire check ===");
    }
} 