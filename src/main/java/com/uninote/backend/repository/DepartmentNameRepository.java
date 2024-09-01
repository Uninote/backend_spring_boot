package com.uninote.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.DepartmentName;

@Repository
public interface DepartmentNameRepository extends JpaRepository<DepartmentName, Long> {
        Optional<DepartmentName> findByName(String name);
        
        @Query("SELECT dn FROM Department d " +
           "JOIN d.departmentNames dn " +
           "JOIN dn.language l " +
           "WHERE d.university.id = :universityId " +
           "AND l.code = :languageCode")
    
        List<DepartmentName> findDepartmentNamesByUniversityIdAndLanguageCode(
                @Param("universityId") Long universityId, 
                @Param("languageCode") String languageCode);
}
