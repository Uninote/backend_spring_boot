/*package com.uninote.backend.config.security;

import com.uninote.backend.filter.APIKeyFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private APIKeyFilter apiKeyFilter;

    @Autowired
    private CustomPermissionEvaluator customPermissionEvaluator;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeRequests()
            .antMatchers(HttpMethod.GET, "/notes/**").access("@customPermissionEvaluator.hasPermission(request, 'NOTE_READ')")
            .antMatchers(HttpMethod.POST, "/notes/**").access("@customPermissionEvaluator.hasPermission(request, 'NOTE_WRITE')")
            .antMatchers("/courses/**").access("@customPermissionEvaluator.hasPermission(request, 'COURSE_READ')")
            .antMatchers("/universities/**").access("@customPermissionEvaluator.hasPermission(request, 'UNIVERSITY_READ')")
            .antMatchers("/departments/**").access("@customPermissionEvaluator.hasPermission(request, 'DEPARTMENT_READ')")
            .anyRequest().permitAll()
            .and()
            .csrf().disable(); // Disable CSRF for simplicity, enable it for production
    }

    @Bean
    public DefaultMethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(customPermissionEvaluator);
        return expressionHandler;
    }
}*/
