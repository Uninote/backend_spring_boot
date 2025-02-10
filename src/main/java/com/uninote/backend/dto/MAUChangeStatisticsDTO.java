package com.uninote.backend.dto;

public class MAUChangeStatisticsDTO {
    private Double lastMonthChange;
    private Double lastQuarterChange;
    private Double lastSixMonthsChange;
    private Double lastYearChange;

    public MAUChangeStatisticsDTO(Double lastMonthChange, Double lastQuarterChange, Double lastSixMonthsChange, Double lastYearChange) {
        this.lastMonthChange = lastMonthChange;
        this.lastQuarterChange = lastQuarterChange;
        this.lastSixMonthsChange = lastSixMonthsChange;
        this.lastYearChange = lastYearChange;
    }

    public Double getLastMonthChange() {
        return lastMonthChange;
    }

    public void setLastMonthChange(Double lastMonthChange) {
        this.lastMonthChange = lastMonthChange;
    }

    public Double getLastQuarterChange() {
        return lastQuarterChange;
    }

    public void setLastQuarterChange(Double lastQuarterChange) {
        this.lastQuarterChange = lastQuarterChange;
    }

    public Double getLastSixMonthsChange() {
        return lastSixMonthsChange;
    }

    public void setLastSixMonthsChange(Double lastSixMonthsChange) {
        this.lastSixMonthsChange = lastSixMonthsChange;
    }

    public Double getLastYearChange() {
        return lastYearChange;
    }

    public void setLastYearChange(Double lastYearChange) {
        this.lastYearChange = lastYearChange;
    }
}
