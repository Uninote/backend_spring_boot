package com.uninote.backend.repository;

import com.uninote.backend.entity.Note;
import com.uninote.backend.entity.NoteClick;
import com.uninote.backend.entity.User;
import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    @Query("SELECT n FROM Note n WHERE n.user = :user AND n.course = :course")
    List<Note> findByUserAndCourse(@Param("user") User user, @Param("course") Course course);

    @Query("SELECT n FROM Note n WHERE n.user = :user AND n.course.department.university = :university")
    List<Note> findByUserAndUniversity(@Param("user") User user, @Param("university") University university);

    @Query("SELECT n FROM Note n WHERE n.user = :user AND n.course.department = :department")
    List<Note> findByUserAndDepartment(@Param("user") User user, @Param("department") Department department);

    @Query("SELECT n FROM Note n WHERE n.user = :user AND n.course.department = :department AND n.course.semester = :semester")
    List<Note> findByUserAndDepartmentAndSemester(@Param("user") User user, @Param("department") Department department, @Param("semester") int semester);

    @Query("SELECT n FROM Note n WHERE n.course.department.id = :departmentId AND n.course.semester = :semester")
    List<Note> findByDepartmentAndSemester(@Param("departmentId") Long departmentId, @Param("semester") int semester);
    
    Optional<Note> findById(Long id);

    List<Note> findByUser(User user);

    List<Note> findByCourse(Course course);

    List<Note> findByTitle(String title);

    List<Note> findByCourse_Department_Id(Long departmentId);

    List<Note> findByCourse_Department_University_Id(Long universityId);

    List<Note> findByCourse_Semester(int semester);



    @Query("SELECT n FROM Note n WHERE n.isPublic = true")
    List<Note> findPublicNotes();

    @Query("SELECT n FROM Note n WHERE n.isPublic = true AND n.user = :user AND n.course.department = :department")
    List<Note> findPublicNotesByUserAndDepartment(@Param("user") User user, @Param("department") Department department);

    @Query("SELECT n FROM Note n WHERE n.isPublic = true AND n.course.department = :department")
    List<Note> findPublicNotesByDepartment(@Param("department") Department department);

    @Query("SELECT n FROM Note n WHERE n.isPublic = true AND n.course.department.university = :university")
    List<Note> findPublicNotesByUniversity(@Param("university") University university);

    @Query("SELECT n FROM Note n WHERE n.isPublic = true AND n.course = :course")
    List<Note> findPublicNotesByCourse(@Param("course") Course course);

    @Query("SELECT n FROM Note n WHERE n.isPublic = true AND n.course.department.id = :departmentId AND n.course.semester = :semester")
    List<Note> findPublicNotesByDepartmentAndSemester(@Param("departmentId") Long departmentId, @Param("semester") int semester);

    @Query("SELECT n FROM Note n WHERE n.isPublic = true AND n.user = :user AND n.course.department.university = :university")
    List<Note> findPublicNotesByUserAndUniversity(@Param("user") User user, @Param("university") University university);

    @Query("SELECT n FROM Note n WHERE n.isPublic = true AND n.user = :user AND n.course = :course")
    List<Note> findPublicNotesByUserAndCourse(@Param("user") User user, @Param("course") Course course);

    @Query("SELECT n FROM Note n WHERE n.isPublic = true AND n.user = :user AND n.course.department.id = :departmentId AND n.course.semester = :semester")
    List<Note> findPublicNotesByUserAndDepartmentAndSemester(@Param("user") User user, @Param("departmentId") Long departmentId, @Param("semester") int semester);

    @Query("SELECT COUNT(n) FROM Note n WHERE n.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    
}
