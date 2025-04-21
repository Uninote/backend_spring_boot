package com.uninote.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;

@Service
public class TutieAiIntegrationService {

    private final RestTemplate restTemplate;

    @Value("${django.api.base-url}")
    private String djangoBaseUrl;

    public TutieAiIntegrationService() {
        this.restTemplate = new RestTemplate();
    }

    public String callDjangoEndpoint() {
        String endpointUrl = djangoBaseUrl + "/your-endpoint/";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer your_token_here"); // Optional

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            endpointUrl,
            HttpMethod.GET,
            entity,
            String.class
        );

        return response.getBody();
    }
}
