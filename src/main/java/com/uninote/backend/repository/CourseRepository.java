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


    @Query(value = "SELECT DISTINCT c.course_id AS id, cn.course_name AS name " +
               "FROM courses c " +
               "JOIN course_names cn ON c.course_id = cn.course_id " +
               "JOIN questions q ON c.course_id = q.course_id " +
               "JOIN languages l ON cn.language_id = l.language_id " +
               "WHERE l.language_code = :language " +
               "AND c.department_id = :departmentId " +
               "AND c.semester = :semester " +
               "AND q.question_id IS NOT NULL",
       nativeQuery = true)
List<CourseProjection> findCoursesWithQuestionsByDepartmentSemesterAndLanguage(
    @Param("departmentId") Long departmentId, 
    @Param("semester") int semester,
    @Param("language") String language);

}
