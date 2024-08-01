package com.uninote.backend.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uninote.backend.entity.CourseName;

@Repository
public interface CourseNameRepository extends JpaRepository<CourseName, Long> {
    // Additional query methods can be defined here if needed
}
