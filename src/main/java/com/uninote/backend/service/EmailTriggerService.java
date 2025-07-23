package com.uninote.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.uninote.backend.entity.EmailEvent;
import com.uninote.backend.repository.EmailEventRepository;

@Service
public class EmailTriggerService {

    @Autowired
    private ResendEmailService resendEmailService;
    @Autowired
    private EmailEventRepository emailEventRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Send emails to users who meet the given condition (dynamic SQL)
     * @param conditionSql SQL to select users (must return id, email, and optionally name)
     * @param subject Email subject (supports {{user.name}})
     * @param html Email HTML body (supports {{user.name}})
     */
    public void sendConditionalEmails(String conditionSql, String subject, String html) {
        // 1. Execute dynamic SQL to get eligible users
        List<Map<String, Object>> users = jdbcTemplate.queryForList(conditionSql);

        for (Map<String, Object> user : users) {
            Long userId = ((Number) user.get("id")).longValue();
            String email = (String) user.get("email");
            String name = user.containsKey("name") ? (String) user.get("name") : "";

            // 2. Check if already sent
            if (emailEventRepository.existsByUserIdAndConditionSql(userId, conditionSql)) continue;

            // 3. Replace placeholders
            String personalizedSubject = subject.replace("{{user.name}}", name);
            String personalizedHtml = html.replace("{{user.name}}", name);

            // 4. Send email
            try {
                resendEmailService.sendEmail(email, personalizedSubject, personalizedHtml);
                // 5. Log event
                emailEventRepository.save(new EmailEvent(userId, conditionSql, LocalDateTime.now()));
            } catch (Exception e) {
                // Handle error (log, retry, etc.)
                e.printStackTrace();
            }
        }
    }
} 