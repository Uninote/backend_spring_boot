package com.uninote.backend.entity;

public class PlanLimits {

    public static int getDailyChatLimit(SubscriptionPlan plan) {
        if (plan == SubscriptionPlan.FREE) {
            return 1;
        } else if (plan == SubscriptionPlan.BASIC) {
            return Integer.MAX_VALUE;
        } else {
            return Integer.MAX_VALUE;
        }
    }

    public static int getMaxSpaces(SubscriptionPlan plan) {
        if (plan == SubscriptionPlan.FREE) {
            return 0;
        } else if (plan == SubscriptionPlan.BASIC) {
            return Integer.MAX_VALUE;
        } else if (plan == SubscriptionPlan.PRO) {
            return Integer.MAX_VALUE;
        } else {
            return Integer.MAX_VALUE;
        }
    }

    public static int getMaxMessagesPerChat(SubscriptionPlan plan) {
        if (plan == SubscriptionPlan.FREE) {
            return 5;
        } else if (plan == SubscriptionPlan.BASIC) {
            return Integer.MAX_VALUE;
        } else {
            return Integer.MAX_VALUE;
        }
    }

    
}
