package com.uninote.backend.dto;

import java.util.Set;



public class UniversityDTO {
    private Long id;
    private String location;
    private Set<UniversityNameDTO> universityNames;
    private Set<Long> departmentIds;

    public UniversityDTO(Long id, String location, Set<UniversityNameDTO> universityNames,  Set<Long> departmentIds) {
        this.id = id;
        this.location = location;
        this.universityNames = universityNames;
        this.departmentIds = departmentIds;
    }

    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Set<UniversityNameDTO> getUniversityNames() {
        return universityNames;
    }

    public void setUniversityNames(Set<UniversityNameDTO> universityNames) {
        this.universityNames = universityNames;
    }

    public Set<Long> getDepartmentIds() {
        return departmentIds;
    }

    public void setDepartmentIds(Set<Long> departmentIds) {
        this.departmentIds = departmentIds;
    }   
}
