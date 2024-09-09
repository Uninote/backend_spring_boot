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


        @Query(value = "SELECT DISTINCT d.department_id AS id, dn.DEPARTMENT_FULL_NAME AS fullName, dn.DEPARTMENT_NAME AS name , d.NUMBER_OF_SEMESTERS AS semesters " +
               "FROM departments d " +
               "JOIN universities u ON d.university_id = u.university_id " +
               "JOIN courses c ON d.department_id = c.department_id " +
               "JOIN questions q ON c.course_id = q.course_id " +
               "JOIN department_names dn ON d.department_id = dn.department_id " +
               "JOIN languages l ON dn.language_id = l.language_id " +
               "WHERE l.language_code = :language " +
               "AND d.university_id = :universityId " +
               "AND q.question_id IS NOT NULL",
       nativeQuery = true)
    List<DepartmentProjection> findDepartmentsWithQuestionsByUniversityAndLanguage(@Param("universityId") Long universityId, @Param("language") String language);

    @Query(value = "SELECT LISTAGG(DISTINCT c.semester, ',') WITHIN GROUP (ORDER BY c.semester) AS semesters " +
                "FROM departments d " +
                "JOIN courses c ON d.department_id = c.department_id " +
                "JOIN questions q ON c.course_id = q.course_id " +
                "WHERE d.department_id = :departmentId " +
                "AND q.question_id IS NOT NULL",
        nativeQuery = true)
    String findSemestersWithQuestionsByDepartment(@Param("departmentId") Long departmentId);


}   


