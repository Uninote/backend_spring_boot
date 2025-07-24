package com.uninote.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uninote.backend.entity.EmailTemplate;
import com.uninote.backend.repository.EmailTemplateRepository;

@Service
public class EmailSchedulerService {
    @Autowired
    private EmailTemplateRepository emailTemplateRepository;
    @Autowired
    private EmailTriggerService emailTriggerService;

    //@Scheduled(fixedRate = 30000) // every 30 seconds
    public void processEmailTemplates() {
        List<EmailTemplate> templates = emailTemplateRepository.findByEnabledTrue();
        for (EmailTemplate template : templates) {
            if (Boolean.TRUE.equals(template.getIsActive())) {
                emailTriggerService.sendConditionalEmails(
                    template.getConditionSql(),
                    template.getSubject(),
                    template.getBody()
                );
            }
        }
    }
} 