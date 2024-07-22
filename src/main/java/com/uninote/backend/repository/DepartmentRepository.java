package com.uninote.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.DepartmentName;
import com.uninote.backend.entity.DepartmentNameId;
import com.uninote.backend.entity.User;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findById(Long departmentId);
}   


