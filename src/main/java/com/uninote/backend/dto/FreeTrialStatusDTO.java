package com.uninote.backend.dto;

import java.time.LocalDateTime;

public class FreeTrialStatusDTO {
    private boolean hasUsedFreeTrial;
    private boolean freeTrialEnded;
    private boolean hasUsedChatAfterTrial;
    private LocalDateTime freeTrialEndDate;

    public FreeTrialStatusDTO(boolean hasUsedFreeTrial, boolean freeTrialEnded, boolean hasUsedChatAfterTrial, LocalDateTime freeTrialEndDate) {
        this.hasUsedFreeTrial = hasUsedFreeTrial;
        this.freeTrialEnded = freeTrialEnded;
        this.hasUsedChatAfterTrial = hasUsedChatAfterTrial;
        this.freeTrialEndDate = freeTrialEndDate;
    }

    public boolean isHasUsedFreeTrial() {
        return hasUsedFreeTrial;
    }

    public void setHasUsedFreeTrial(boolean hasUsedFreeTrial) {
        this.hasUsedFreeTrial = hasUsedFreeTrial;
    }

    public boolean isFreeTrialEnded() {
        return freeTrialEnded;
    }

    public void setFreeTrialEnded(boolean freeTrialEnded) {
        this.freeTrialEnded = freeTrialEnded;
    }

    public boolean isHasUsedChatAfterTrial() {
        return hasUsedChatAfterTrial;
    }

    public void setHasUsedChatAfterTrial(boolean hasUsedChatAfterTrial) {
        this.hasUsedChatAfterTrial = hasUsedChatAfterTrial;
    }

    public LocalDateTime getFreeTrialEndDate() {
        return freeTrialEndDate;
    }

    public void setFreeTrialEndDate(LocalDateTime freeTrialEndDate) {
        this.freeTrialEndDate = freeTrialEndDate;
    }
} 