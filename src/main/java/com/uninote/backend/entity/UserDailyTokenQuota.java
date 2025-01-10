package com.uninote.backend.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Date;

@Entity
@Table(name = "user_daily_token_quota")
public class UserDailyTokenQuota {  

    @EmbeddedId
    private UserDailyTokenQuotaId id;


    @Column(name = "tokens_used", nullable = false)
    private int tokensUsed;

    

    public UserDailyTokenQuotaId getId() {
        return this.id;
    }

    public void setId(UserDailyTokenQuotaId id) {
        this.id=  id;
    }

    public int getTokensUsed() {
        return tokensUsed;
    }

    public void setTokensUsed(int tokensUsed) {
        this.tokensUsed = tokensUsed;
    }
}

    