package com.uninote.backend.entity;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class DepartmentNameId implements Serializable {

    private Long departmentId;
    private Long languageId;

   
    public DepartmentNameId() {}

    public DepartmentNameId(Long departmentId, Long languageId) {
        this.departmentId = departmentId;
        this.languageId = languageId;
    }

    
    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Long getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Long languageId) {
        this.languageId = languageId;
    }

    
    @Override
    public int hashCode() {
        return Objects.hash(departmentId, languageId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DepartmentNameId that = (DepartmentNameId) o;
        return Objects.equals(departmentId, that.departmentId) &&
               Objects.equals(languageId, that.languageId);
    }
}
