package com.uninote.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.Department;

public interface CourseRepository extends JpaRepository<Course,Long> {

    Optional<Course> findById(String id);
    List<Course> findByDepartmentAndSemester(Department department, int semester);
}
