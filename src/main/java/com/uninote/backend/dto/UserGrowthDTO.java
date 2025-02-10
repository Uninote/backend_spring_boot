package com.uninote.backend.dto;

public class UserGrowthDTO {
    private String month;
    private Long totalUsers;

    public UserGrowthDTO(String month, Long totalUsers) {
        this.month = month;
        this.totalUsers = totalUsers;
    }

    // Getter for month
    public String getMonth() {
        return month;
    }

    // Setter for month
    public void setMonth(String month) {
        this.month = month;
    }

    // Getter for totalUsers
    public Long getTotalUsers() {
        return totalUsers;
    }

    // Setter for totalUsers
    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }
}
