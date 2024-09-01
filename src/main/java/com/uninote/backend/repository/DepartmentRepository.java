package com.uninote.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import com.uninote.backend.dto.DepartmentDTO;
import com.uninote.backend.dto.DepartmentNameDTO;
import com.uninote.backend.entity.Department;
import com.uninote.backend.interfaceProjection.DepartmentProjection;


@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findById(Long departmentId);

    @Query("SELECT new com.uninote.backend.dto.DepartmentNameDTO(dn.id.departmentId, dn.name, l.code, dn.fullName) " +
       "FROM DepartmentName dn " +
       "JOIN dn.language l " +
       "WHERE dn.id.departmentId = :departmentId")
    List<DepartmentNameDTO> findNamesById(@Param("departmentId") Long departmentId);

    List<Department> findByUniversityId(Long universityId);

    @Query("SELECT d.id AS id, dn.fullName AS fullName, dn.name AS name, l.code AS languageCode, d.semesters AS semesters " +
           "FROM Department d " +
           "JOIN d.departmentNames dn " +
           "JOIN dn.language l " +
           "WHERE d.university.id = :universityId " +
           "AND l.code = :languageCode")
    List<DepartmentProjection> findDepartmentProjectionsByUniversityIdAndLanguageCode(
        @Param("universityId") Long universityId, 
        @Param("languageCode") String languageCode);
}   


