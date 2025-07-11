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

    // Changed from 30 seconds to 1 hour
    @Scheduled(fixedRate = 3600000)
    public void hourlyQuestionnaireCheck() {
        try {
            questionnaireTriggerService.triggerAllActiveQuestionnaires(false);
        } catch (Exception e) {
            // Silent error handling
        }
    }

    // Changed from every 2 minutes to once per day at 9 AM
    @Scheduled(cron = "0 0 9 * * ?") 
    public void dailyQuestionnaireCheck() {
        try {
            questionnaireTriggerService.triggerAllActiveQuestionnaires(false);
        } catch (Exception e) {
            // Silent error handling
        }
    }

    // Changed from 1 minute to 6 hours
    @Scheduled(fixedRate = 21600000)
    public void sixHourlyQuestionnaireCheck() {
        try {
            questionnaireTriggerService.triggerAllActiveQuestionnaires(false);
        } catch (Exception e) {
            // Silent error handling
        }
    }
} 