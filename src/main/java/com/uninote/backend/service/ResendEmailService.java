package com.uninote.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

@Service
public class ResendEmailService {

    @Value("${resend.api.key}")
    private String apiKey;

    public String sendEmail(String to, String subject, String html) throws ResendException {
        Resend resend = new Resend(apiKey);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("Uninote Team <contact@uninote.gr>")  
                .to(to)
                .subject(subject)
                .html(html)
                .build();

        CreateEmailResponse data = resend.emails().send(params);
        return data.getId();
    }
}