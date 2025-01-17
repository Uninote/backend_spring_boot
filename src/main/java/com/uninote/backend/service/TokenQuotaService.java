package com.uninote.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.uninote.backend.entity.UserDailyTokenQuota;
import com.uninote.backend.entity.UserDailyTokenQuotaId;
import com.uninote.backend.repository.UserDailyTokenQuotaRepository;

import java.sql.Date;
import java.time.LocalDate;

@Service
public class TokenQuotaService {

    private static final Logger logger = LoggerFactory.getLogger(TutieService.class);


    @Autowired
    private UserDailyTokenQuotaRepository tokenQuotaRepository;

    @Value("${app.daily-token-quota:300000}") 
    private int dailyTokenQuota;

    public boolean hasSufficientQuota(Long userId, int tokensNeeded) {
        logger.info("CALCULATING USER QUOTA");
        LocalDate today = LocalDate.now();
        Date quotaDate = Date.valueOf(today);

        UserDailyTokenQuota quota = tokenQuotaRepository.findByIdUserIdAndIdQuotaDate(userId, quotaDate)
                .orElseGet(() -> createNewQuota(userId, quotaDate));
        logger.info("GOT USER QUOTA");
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
        if (userId == null || quotaDate == null) {
            throw new IllegalArgumentException("userId and quotaDate must not be null");
        }
        UserDailyTokenQuotaId id = new UserDailyTokenQuotaId();
        id.setQuotaDate(quotaDate);
        id.setUserId(userId);
        UserDailyTokenQuota quota = new UserDailyTokenQuota();
        quota.setId(id);
        quota.setTokensUsed(0);
        return tokenQuotaRepository.save(quota);
    }
}
