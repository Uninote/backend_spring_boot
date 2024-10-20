package com.uninote.backend.config.security;
/* 
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class AuthenticationService {

    private final String apiKey;

    public AuthenticationService(@Value("${app.api.key}") String apiKey) {
        this.apiKey = apiKey;
    }

    public Authentication getAuthentication(HttpServletRequest request) {
        String requestApiKey = request.getHeader("X-API-KEY");
        if (requestApiKey == null || !requestApiKey.equals(this.apiKey)) {
            throw new BadCredentialsException("Invalid API Key");
        }

        return new ApiKeyAuthentication(requestApiKey, AuthorityUtils.NO_AUTHORITIES);
    }
}
*/