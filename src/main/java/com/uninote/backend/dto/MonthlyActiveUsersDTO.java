package com.uninote.backend.dto;


public class MonthlyActiveUsersDTO {
    private String month;
    private Long activeUsers;

    public MonthlyActiveUsersDTO(String month, Long activeUsers) {
        this.month = month;
        this.activeUsers = activeUsers;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public Long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(Long activeUsers) {
        this.activeUsers = activeUsers;
    }
}
