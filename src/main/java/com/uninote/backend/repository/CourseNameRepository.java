package com.uninote.backend.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uninote.backend.entity.CourseName;

import java.util.List;

@Repository
public interface CourseNameRepository extends JpaRepository<CourseName, Long> {
    Optional<CourseName> findByCourseIdAndLanguageId(Long courseId, Long languageId);

    List<CourseName> findByLanguageIdAndCourse_DepartmentId(Long languageId, Long departmentId);

}
