package com.uninote.backend.dto;

import java.util.Set;

public class UniversityDTO {
    private Long id;
    private String location;
    private Set<UniversityNameDTO> universityNames;

    public UniversityDTO(Long id, String location, Set<UniversityNameDTO> universityNames) {
        this.id = id;
        this.location = location;
        this.universityNames = universityNames;
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
}
