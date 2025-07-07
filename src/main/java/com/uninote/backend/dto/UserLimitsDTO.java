package com.uninote.backend.dto;

import com.uninote.backend.entity.SubscriptionPlan;

public class UserLimitsDTO {
    private SubscriptionPlan subscriptionPlan;
    private int dailyChatLimit;
    private int dailyChatUsed;
    private int dailyChatRemaining;
    private int dailySpaceLimit;
    private int dailySpaceUsed;
    private int dailySpaceRemaining;
    private int dailyMessageLimit;
    private int dailyMessageUsed;
    private int dailyMessageRemaining;

    public UserLimitsDTO() {}

    public UserLimitsDTO(SubscriptionPlan subscriptionPlan, 
                        int dailyChatLimit, int dailyChatUsed,
                        int dailySpaceLimit, int dailySpaceUsed,
                        int dailyMessageLimit, int dailyMessageUsed) {
        this.subscriptionPlan = subscriptionPlan;
        this.dailyChatLimit = dailyChatLimit;
        this.dailyChatUsed = dailyChatUsed;
        this.dailyChatRemaining = Math.max(0, dailyChatLimit - dailyChatUsed);
        this.dailySpaceLimit = dailySpaceLimit;
        this.dailySpaceUsed = dailySpaceUsed;
        this.dailySpaceRemaining = Math.max(0, dailySpaceLimit - dailySpaceUsed);
        this.dailyMessageLimit = dailyMessageLimit;
        this.dailyMessageUsed = dailyMessageUsed;
        this.dailyMessageRemaining = Math.max(0, dailyMessageLimit - dailyMessageUsed);
    }

    // Getters and Setters
    public SubscriptionPlan getSubscriptionPlan() {
        return subscriptionPlan;
    }

    public void setSubscriptionPlan(SubscriptionPlan subscriptionPlan) {
        this.subscriptionPlan = subscriptionPlan;
    }

    public int getDailyChatLimit() {
        return dailyChatLimit;
    }

    public void setDailyChatLimit(int dailyChatLimit) {
        this.dailyChatLimit = dailyChatLimit;
    }

    public int getDailyChatUsed() {
        return dailyChatUsed;
    }

    public void setDailyChatUsed(int dailyChatUsed) {
        this.dailyChatUsed = dailyChatUsed;
    }

    public int getDailyChatRemaining() {
        return dailyChatRemaining;
    }

    public void setDailyChatRemaining(int dailyChatRemaining) {
        this.dailyChatRemaining = dailyChatRemaining;
    }

    public int getDailySpaceLimit() {
        return dailySpaceLimit;
    }

    public void setDailySpaceLimit(int dailySpaceLimit) {
        this.dailySpaceLimit = dailySpaceLimit;
    }

    public int getDailySpaceUsed() {
        return dailySpaceUsed;
    }

    public void setDailySpaceUsed(int dailySpaceUsed) {
        this.dailySpaceUsed = dailySpaceUsed;
    }

    public int getDailySpaceRemaining() {
        return dailySpaceRemaining;
    }

    public void setDailySpaceRemaining(int dailySpaceRemaining) {
        this.dailySpaceRemaining = dailySpaceRemaining;
    }

    public int getDailyMessageLimit() {
        return dailyMessageLimit;
    }

    public void setDailyMessageLimit(int dailyMessageLimit) {
        this.dailyMessageLimit = dailyMessageLimit;
    }

    public int getDailyMessageUsed() {
        return dailyMessageUsed;
    }

    public void setDailyMessageUsed(int dailyMessageUsed) {
        this.dailyMessageUsed = dailyMessageUsed;
    }

    public int getDailyMessageRemaining() {
        return dailyMessageRemaining;
    }

    public void setDailyMessageRemaining(int dailyMessageRemaining) {
        this.dailyMessageRemaining = dailyMessageRemaining;
    }
} 