package com.uninote.backend.entity;

import java.io.Serializable;
import java.sql.Date;
import java.util.Objects;

import javax.persistence.Column;

public class UserDailyTokenQuotaId implements Serializable {


    @Column(name = "user_id", nullable = false )
    private Long userId;

    @Column(name = "quota_date", nullable = false)
    private Date quotaDate;



    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getQuotaDate() {
        return quotaDate;
    }

    public void setQuotaDate(Date quotaDate) {
        this.quotaDate = quotaDate;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDailyTokenQuotaId that = (UserDailyTokenQuotaId) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(quotaDate, that.quotaDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash( userId, quotaDate);
    }
    
}
