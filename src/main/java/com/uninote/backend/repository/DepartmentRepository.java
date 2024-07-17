package com.uninote.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.DepartmentName;
import com.uninote.backend.entity.DepartmentNameId;

@Repository
public interface DepartmentRepository extends JpaRepository<DepartmentName, DepartmentNameId> {
    List<DepartmentName> findByDepartmentId(Long departmentId);
    List<DepartmentName> findByLanguageId(Long languageId);
    DepartmentName findByDepartmentIdAndLanguageId(Long departmentId, Long languageId);
    List<DepartmentName> findByNameContainingIgnoreCase(String name);
    List<DepartmentName> findByFullName(String fullName);
    List<DepartmentName> findByName(String name);
}   


