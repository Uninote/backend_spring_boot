package com.uninote.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uninote.backend.dto.NoteDTO;
import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.Note;
import com.uninote.backend.entity.User;

import java.util.List;


@Repository
public interface NoteRepository extends JpaRepository<Note,Long>{
    Optional<Note> findById(Long id);

    List<NoteDTO> findByUser(User user);

    List<Note> findByCourse(Course course);
    List<Note> findByTitle(String title);
    List<Note> findByUserAndCourse(User user, Course course);

    @Query("SELECT n FROM Note n WHERE n.course.department.id = :departmentId")
    List<Note> findByDepartment(@Param("departmentId") Long departmentId);

    @Query("SELECT n FROM Note n WHERE n.course.semester = :semester")
    List<Note> findBySemester(@Param("semester") int semester);

    @Query("SELECT n FROM Note n WHERE n.course.department.university.id = :universityId")
    List<Note> findByUniversity(@Param("universityId") Long universityId);

}
