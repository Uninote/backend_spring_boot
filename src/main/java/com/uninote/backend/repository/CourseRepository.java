package com.uninote.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uninote.backend.entity.Course;

public interface CourseRepository extends JpaRepository<Course,Long> {

    
}
