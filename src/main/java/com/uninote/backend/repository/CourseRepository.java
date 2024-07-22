package com.uninote.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uninote.backend.entity.Course;

public interface CourseRepository extends JpaRepository<Course,Long> {

    Optional<Course> findById(String id);
}
