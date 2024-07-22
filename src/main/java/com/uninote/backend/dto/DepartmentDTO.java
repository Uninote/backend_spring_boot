package com.uninote.backend.dto;

import java.util.Set;

public class DepartmentDTO {
    private Long id;
    private Long universityId;
    private String code;
    private int semesters;
    private Set<Long> courseIds;
    private Set<DepartmentNameDTO> departmentNames;
    public DepartmentDTO() {
    }

    public DepartmentDTO(Long id, Long universityId, String code, int semesters, Set<Long> courseIds, Set<DepartmentNameDTO> departmentNames) {
        this.id = id;
        this.universityId = universityId;
        this.code = code;
        this.semesters = semesters;
        this.courseIds = courseIds;
        this.departmentNames = departmentNames;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUniversityId() {
        return universityId;
    }

    public void setUniversityId(Long universityId) {
        this.universityId = universityId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getSemesters() {
        return semesters;
    }

    public void setSemesters(int semesters) {
        this.semesters = semesters;
    }

    public Set<Long> getCourseIds() {
        return courseIds;
    }

    public void setCourseIds(Set<Long> courseIds) {
        this.courseIds = courseIds;
    }
    public Set<DepartmentNameDTO> getDepartmentNames(){
        return departmentNames;
    }

    public void setDepartmentNames(Set<DepartmentNameDTO> departmentNameDTOs){
        this.departmentNames = departmentNameDTOs;
    }
}
