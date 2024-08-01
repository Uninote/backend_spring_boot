/*package com.uninote.backend.config.security;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Set;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (targetDomainObject instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) targetDomainObject;
            String permissionStr = (String) permission;
            Set<String> permissions = (Set<String>) request.getAttribute("permissions");
            return permissions != null && permissions.contains(permissionStr);
        }
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        // Implement your logic if you need to evaluate permission based on targetId and targetType
        return false;
    }
}*/
