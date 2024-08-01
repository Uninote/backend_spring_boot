package com.uninote.backend.service;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class APIKeyStore {

    private static final Map<String, Set<String>> apiKeyPermissions = new HashMap<>();

    @PostConstruct
    public void init() {
        
        apiKeyPermissions.put("I8TM4vbfmGVDkPM5le2aT23j56sFMWO2xgOA1oWz664=", Set.of("COURSE_READ", "UNIVERSITY_READ", "DEPARTMENT_READ"));
    }

    public Set<String> getPermissions(String apiKey) {
        return apiKeyPermissions.get(apiKey);
    }

    public void addKey(String apiKey, Set<String> permissions) {
        apiKeyPermissions.put(apiKey, permissions);
    }

    public void removeKey(String apiKey) {
        apiKeyPermissions.remove(apiKey);
    }
}
