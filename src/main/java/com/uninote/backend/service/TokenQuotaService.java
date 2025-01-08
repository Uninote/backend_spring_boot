package com.uninote.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.uninote.backend.entity.UserDailyTokenQuota;
import com.uninote.backend.repository.UserDailyTokenQuotaRepository;

import java.sql.Date;
import java.time.LocalDate;

@Service
public class TokenQuotaService {

    @Autowired
    private UserDailyTokenQuotaRepository tokenQuotaRepository;

    @Value("${app.daily-token-quota:10000}") 
    private int dailyTokenQuota;

    public boolean hasSufficientQuota(Long userId, int tokensNeeded) {
        LocalDate today = LocalDate.now();
        Date quotaDate = Date.valueOf(today);

        UserDailyTokenQuota quota = tokenQuotaRepository.findByIdUserIdAndIdQuotaDate(userId, quotaDate)
                .orElseGet(() -> createNewQuota(userId, quotaDate));

        return (quota.getTokensUsed() + tokensNeeded) <= dailyTokenQuota;
    }

    
    public void updateTokenUsage(Long userId, int tokensUsed) {
        LocalDate today = LocalDate.now();
        Date quotaDate = Date.valueOf(today);

        UserDailyTokenQuota quota = tokenQuotaRepository.findByIdUserIdAndIdQuotaDate(userId, quotaDate)
                .orElseGet(() -> createNewQuota(userId, quotaDate));

        quota.setTokensUsed(quota.getTokensUsed() + tokensUsed);
        tokenQuotaRepository.save(quota);
    }

    
    private UserDailyTokenQuota createNewQuota(Long userId, Date quotaDate) {
        UserDailyTokenQuota quota = new UserDailyTokenQuota();
        quota.getId().setUserId(userId);
        quota.getId().setQuotaDate(quotaDate);
        quota.setTokensUsed(0);
        return tokenQuotaRepository.save(quota);
    }
}
