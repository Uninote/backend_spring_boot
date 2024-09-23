package com.uninote.backend.dto;

public class NoteMetricDTO {
    private Long deptId;
    private String deptName;
    private String uniName;
    private Double percentageCoursesWithNotes;

    // Constructor
    public NoteMetricDTO(Long deptId, String deptName, String uniName, Double percentageCoursesWithNotes) {
        this.deptId = deptId;
        this.deptName = deptName;
        this.uniName = uniName;
        this.percentageCoursesWithNotes = percentageCoursesWithNotes;
    }

    // Getters and setters
    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getUniName() {
        return uniName;
    }

    public void setUniName(String uniName) {
        this.uniName = uniName;
    }

    public Double getPercentageCoursesWithNotes() {
        return percentageCoursesWithNotes;
    }

    public void setPercentageCoursesWithNotes(Double percentageCoursesWithNotes) {
        this.percentageCoursesWithNotes = percentageCoursesWithNotes;
    }
}
