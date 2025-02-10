package com.uninote.backend.dto;

public class ContentCoverageDTO {
    private String university;
    private String department;
    private double contentCoverage;

    public ContentCoverageDTO(String university, String department, double contentCoverage) {
        this.university = university;
        this.department = department;
        this.contentCoverage = contentCoverage;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public double getContentCoverage() {
        return contentCoverage;
    }

    public void setContentCoverage(double contentCoverage) {
        this.contentCoverage = contentCoverage;
    }
}
