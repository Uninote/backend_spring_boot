package com.uninote.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.DepartmentName;

@Repository
public interface DepartmentNameRepository extends JpaRepository<DepartmentName, Long> {
        Optional<DepartmentName> findByName(String name);

}
