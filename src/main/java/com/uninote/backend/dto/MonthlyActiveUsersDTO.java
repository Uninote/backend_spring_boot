package com.uninote.backend.dto;


public class MonthlyActiveUsersDTO {
    private String month;
    private Long activeUsers;
    private Double activeUserPercentage;

    public MonthlyActiveUsersDTO(String month, Long activeUsers) {
        this.month = month;
        this.activeUsers = activeUsers;
    }


    public MonthlyActiveUsersDTO(String month, long activeUsers, Double activeUserPercentage) {
        this.month = month;
        this.activeUsers = activeUsers;
        this.activeUserPercentage = activeUserPercentage;
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


    public Double getActiveUserPercentage() {
        return activeUserPercentage;
    }

    public void setActiveUserPercentage(Double activeUserPercentage) {
        this.activeUserPercentage = activeUserPercentage;
    }
}
