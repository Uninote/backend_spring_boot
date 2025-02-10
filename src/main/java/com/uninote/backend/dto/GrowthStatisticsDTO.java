package com.uninote.backend.dto;

public class GrowthStatisticsDTO {
    private Long lastWeek;
    private Long lastMonth;
    private Long lastQuarter;
    private Long lastYear;

    public GrowthStatisticsDTO(Long lastWeek, Long lastMonth, Long lastQuarter, Long lastYear) {
        this.lastWeek = lastWeek;
        this.lastMonth = lastMonth;
        this.lastQuarter = lastQuarter;
        this.lastYear = lastYear;
    }

    public Long getLastWeek() {
        return lastWeek;
    }

    public void setLastWeek(Long lastWeek) {
        this.lastWeek = lastWeek;
    }

    public Long getLastMonth() {
        return lastMonth;
    }

    public void setLastMonth(Long lastMonth) {
        this.lastMonth = lastMonth;
    }

    public Long getLastQuarter() {
        return lastQuarter;
    }

    public void setLastQuarter(Long lastQuarter) {
        this.lastQuarter = lastQuarter;
    }

    public Long getLastYear() {
        return lastYear;
    }

    public void setLastYear(Long lastYear) {
        this.lastYear = lastYear;
    }
}
