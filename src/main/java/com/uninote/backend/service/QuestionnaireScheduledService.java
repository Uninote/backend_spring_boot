package com.uninote.backend.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuestionnaireScheduledService {

    @Autowired
    private QuestionnaireTriggerService questionnaireTriggerService;

    @PostConstruct
    public void init() {
        // Service initialized
    }

    // DISABLED: Single scheduled task instead of 3 overlapping ones
    // @Scheduled(fixedRate = 300000) // Every 5 minutes instead of multiple overlapping tasks
    public void questionnaireCheck() {
        try {
            questionnaireTriggerService.triggerAllActiveQuestionnaires(false);
        } catch (Exception e) {
            // Silent error handling
        }
    }
} 