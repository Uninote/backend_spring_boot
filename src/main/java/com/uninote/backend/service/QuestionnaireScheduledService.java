package com.uninote.backend.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class QuestionnaireScheduledService {

    @Autowired
    private QuestionnaireTriggerService questionnaireTriggerService;

    @PostConstruct
    public void init() {
        // Service initialized
    }

    @Scheduled(fixedRate = 30000)
    public void hourlyQuestionnaireCheck() {
        try {
            questionnaireTriggerService.triggerAllActiveQuestionnaires(false);
        } catch (Exception e) {
            // Silent error handling
        }
    }

    @Scheduled(cron = "0 */2 * * * ?") 
    public void dailyQuestionnaireCheck() {
        try {
            questionnaireTriggerService.triggerAllActiveQuestionnaires(false);
        } catch (Exception e) {
            // Silent error handling
        }
    }

    @Scheduled(fixedRate = 60000)
    public void sixHourlyQuestionnaireCheck() {
        try {
            questionnaireTriggerService.triggerAllActiveQuestionnaires(false);
        } catch (Exception e) {
            // Silent error handling
        }
    }
} 