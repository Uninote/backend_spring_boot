/*package com.uninote.backend.filter;

import com.uninote.backend.service.APIKeyStore;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

@Component
public class APIKeyFilter extends OncePerRequestFilter {

    private final APIKeyStore apiKeyStore;

    public APIKeyFilter(APIKeyStore apiKeyStore) {
        this.apiKeyStore = apiKeyStore;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String apiKey = request.getHeader("X-API-KEY");

        if (apiKey == null || apiKeyStore.getPermissions(apiKey) == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Log the permissions for debugging
        Set<String> permissions = apiKeyStore.getPermissions(apiKey);
        System.out.println("API Key Permissions: " + permissions);

        // Set permissions as request attribute for further processing in security config
        request.setAttribute("permissions", permissions);

        filterChain.doFilter(request, response);
    }
}*/
