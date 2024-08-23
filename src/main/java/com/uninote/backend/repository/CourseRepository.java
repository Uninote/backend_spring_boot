package com.uninote.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.Department;
import com.uninote.backend.interfaceProjection.CourseProjection;

public interface CourseRepository extends JpaRepository<Course,Long> {

    Optional<Course> findById(String id);
    List<Course> findByDepartmentAndSemester(Department department, int semester);
    
    @Query("SELECT c.id AS id, cn.name AS name " +
       "FROM Course c " +
       "JOIN c.courseNames cn " +
       "JOIN cn.language l " +
       "WHERE c.department.id = :departmentId " +
       "AND c.semester = :semesterId " +
       "AND cn.id.languageId = (SELECT l.id FROM Language l WHERE l.code = :languageCode)")
List<CourseProjection> findCoursesByDepartmentAndSemesterAndLanguage(
    @Param("departmentId") Long departmentId,
    @Param("semesterId") int semesterId,
    @Param("languageCode") String languageCode);
}
