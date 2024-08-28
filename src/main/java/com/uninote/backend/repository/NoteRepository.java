package com.uninote.backend.repository;

import com.uninote.backend.entity.Note;
import com.uninote.backend.entity.NoteClick;
import com.uninote.backend.entity.User;
import com.uninote.backend.interfaceProjection.NoteProjection;
import com.uninote.backend.dto.NoteDTO;
import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.University;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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


    @Query("SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, n.filename, n.isPublic, " +
       "(SELECT cn.name FROM CourseName cn WHERE cn.course = c AND cn.language.code = 'EN'), " +
       "(SELECT un.name FROM UniversityName un WHERE un.university = d.university AND un.language.code = 'EN'), " +
       "(SELECT dn.name FROM DepartmentName dn WHERE dn.department = d AND dn.language.code = 'EN'), " +
       "n.likes , u.username, u.profileImageUrl, n.createdAt) " +
       "FROM Note n " +
       "JOIN n.course c " +
       "JOIN c.department d " +
       "JOIN n.user u " +
       "WHERE n.user.id = :userId")
    List<NoteDTO> findByUserId(Long userId);

    List<Note> findByCourse(Course course);

    List<Note> findByTitle(String title);

    List<Note> findByCourse_Department_Id(Long departmentId);

    List<Note> findByCourse_Department_University_Id(Long universityId);

    List<Note> findByCourse_Semester(int semester);



    //@Query("SELECT n FROM Note n WHERE n.isPublic = true")
    //List<Note> findPublicNotes();

    @Query(value = "SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, " +
               "n.filename, n.isPublic, cn.name, un.name, dn.name, " +
               "n.likes, u.username, u.profileImageUrl, n.createdAt) " +
               "FROM Note n " +
               "JOIN n.course c " +
               "JOIN c.department d " +
               "JOIN n.user u " +
               "JOIN CourseName cn ON cn.course = c " +
               "JOIN cn.language l " +  
               "JOIN UniversityName un ON un.university = d.university " +
               "JOIN un.language ul " +  
               "JOIN DepartmentName dn ON dn.department = d " +
               "JOIN dn.language dl " +  
               "WHERE l.code = 'EN' AND ul.code = 'EN' AND dl.code = 'EN' " +
               "AND n.isPublic = true ")
Page<NoteDTO> findPublicNotes(Pageable pageable);

    @Query("SELECT n FROM Note n WHERE n.isPublic = true AND n.user = :user AND n.course.department = :department")
    List<Note> findPublicNotesByUserAndDepartment(@Param("user") User user, @Param("department") Department department);

    @Query(value = "SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, " +
               "n.filename, n.isPublic, cn.name, un.name, dn.name, " +
               "n.likes, u.username, u.profileImageUrl, n.createdAt) " +
               "FROM Note n " +
               "JOIN n.course c " +
               "JOIN c.department d " +
               "JOIN n.user u " +
               "JOIN CourseName cn ON cn.course = c " +
               "JOIN cn.language l " +  
               "JOIN UniversityName un ON un.university = d.university " +
               "JOIN un.language ul " +  
               "JOIN DepartmentName dn ON dn.department = d " +
               "JOIN dn.language dl " +  
               "WHERE l.code = 'EN' AND ul.code = 'EN' AND dl.code = 'EN' " +
               "AND n.isPublic = true AND d = :department")
Page<NoteDTO> findPublicNotesByDepartment(@Param("department") Department department,  Pageable pageable);



@Query(value = "SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, " +
               "n.filename, n.isPublic, cn.name, un.name, dn.name, " +
               "n.likes, u.username, u.profileImageUrl, n.createdAt) " +
               "FROM Note n " +
               "JOIN n.course c " +
               "JOIN c.department d " +
               "JOIN n.user u " +
               "JOIN CourseName cn ON cn.course = c " +
               "JOIN cn.language l " +  
               "JOIN UniversityName un ON un.university = d.university " +
               "JOIN un.language ul " +  
               "JOIN DepartmentName dn ON dn.department = d " +
               "JOIN dn.language dl " +  
               "WHERE l.code = 'EN' AND ul.code = 'EN' AND dl.code = 'EN' " +
               "AND n.isPublic = true AND n.course.department.university = :university")
   Page<NoteDTO> findPublicNotesByUniversity(@Param("university") University university, Pageable pageable);

    @Query(value = "SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, " +
               "n.filename, n.isPublic, cn.name, un.name, dn.name, " +
               "n.likes, u.username, u.profileImageUrl, n.createdAt) " +
               "FROM Note n " +
               "JOIN n.course c " +
               "JOIN c.department d " +
               "JOIN n.user u " +
               "JOIN CourseName cn ON cn.course = c " +
               "JOIN cn.language l " +  
               "JOIN UniversityName un ON un.university = d.university " +
               "JOIN un.language ul " +  
               "JOIN DepartmentName dn ON dn.department = d " +
               "JOIN dn.language dl " +  
               "WHERE l.code = 'EN' AND ul.code = 'EN' AND dl.code = 'EN' " +
               "AND n.isPublic = true AND n.course = :course")
    Page<NoteDTO> findPublicNotesByCourse(@Param("course") Course course, Pageable pageable);

    @Query(value = "SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, " +
               "n.filename, n.isPublic, cn.name, un.name, dn.name, " +
               "n.likes, u.username, u.profileImageUrl, n.createdAt) " +
               "FROM Note n " +
               "JOIN n.course c " +
               "JOIN c.department d " +
               "JOIN n.user u " +
               "JOIN CourseName cn ON cn.course = c " +
               "JOIN cn.language l " +  
               "JOIN UniversityName un ON un.university = d.university " +
               "JOIN un.language ul " +  
               "JOIN DepartmentName dn ON dn.department = d " +
               "JOIN dn.language dl " +  
               "WHERE l.code = 'EN' AND ul.code = 'EN' AND dl.code = 'EN' " +
               "AND n.isPublic = true AND n.course.department.id = :departmentId AND n.course.semester = :semester")
    Page<NoteDTO> findPublicNotesByDepartmentAndSemester(@Param("departmentId") Long departmentId, @Param("semester") int semester, Pageable pageable);

    @Query("SELECT n FROM Note n WHERE n.isPublic = true AND n.user = :user AND n.course.department.university = :university")
    List<Note> findPublicNotesByUserAndUniversity(@Param("user") User user, @Param("university") University university);

    @Query("SELECT n FROM Note n WHERE n.isPublic = true AND n.user = :user AND n.course = :course")
    List<Note> findPublicNotesByUserAndCourse(@Param("user") User user, @Param("course") Course course);

    @Query("SELECT n FROM Note n WHERE n.isPublic = true AND n.user = :user AND n.course.department.id = :departmentId AND n.course.semester = :semester")
    List<Note> findPublicNotesByUserAndDepartmentAndSemester(@Param("user") User user, @Param("departmentId") Long departmentId, @Param("semester") int semester);

    @Query("SELECT COUNT(n) FROM Note n WHERE n.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);


    @Query("SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, n.filename, n.isPublic, " +
       "(SELECT cn.name FROM CourseName cn WHERE cn.course = c AND cn.language.code = 'EN'), " +
       "(SELECT un.name FROM UniversityName un WHERE un.university = d.university AND un.language.code = 'EN'), " +
       "(SELECT dn.name FROM DepartmentName dn WHERE dn.department = d AND dn.language.code = 'EN'), " +
       "n.likes, u.username, u.profileImageUrl, n.createdAt) " +
       "FROM NoteSave ns " +
       "JOIN ns.note n " +
       "JOIN n.course c " +
       "JOIN c.department d " +
       "JOIN n.user u " +
       "WHERE ns.user.id = :userId AND ns.isActive = TRUE AND n.isPublic = TRUE")
   List<NoteDTO> findPublicSavedNotesByUserId(@Param("userId") Long userId);

   @Query(value = "SELECT n.note_id AS id, c.course_id AS courseId, u.user_id AS userId, n.title AS title, " +
               "DBMS_LOB.SUBSTR(n.description, 4000, 1) AS description, n.pdf_url AS pdfUrl, n.filename AS filename, " +
               "(SELECT cn.course_name FROM course_names cn " +
               "JOIN languages l ON cn.language_id = l.language_id " +
               "WHERE cn.course_id = c.course_id AND l.language_code = 'EN') AS courseName, " +
               "(SELECT un.university_name FROM university_names un " +
               "JOIN languages l ON un.language_id = l.language_id " +
               "WHERE un.university_id = d.university_id AND l.language_code = 'EN') AS universityName, " +
               "(SELECT dn.department_name FROM department_names dn " +
               "JOIN languages l ON dn.language_id = l.language_id " +
               "WHERE dn.department_id = d.department_id AND l.language_code = 'EN') AS departmentName, " +
               "n.like_count AS likes, u.username AS username, u.profile_image_url AS profileImageUrl, n.created_at AS createdAt " +
               "FROM notes n " +
               "JOIN courses c ON n.course_id = c.course_id " +
               "JOIN departments d ON c.department_id = d.department_id " +
               "JOIN users u ON n.user_id = u.user_id " +
               "WHERE n.is_public = 1 AND u.user_id = :userId " +
               "ORDER BY n.like_count DESC, n.created_at DESC " +
               "FETCH FIRST :limit ROWS ONLY", nativeQuery = true)
    List<NoteProjection> findTopPublicNotesByUser(@Param("userId") Long userId, @Param("limit") int limit);

    
    @Query("SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, " +
       "n.filename, n.isPublic, cn.name, un.name, dn.name, " +
       "n.likes, u.username, u.profileImageUrl, n.createdAt) " +
       "FROM Note n " +
       "JOIN n.course c " +
       "JOIN c.department d " +
       "JOIN n.user u " +
       "JOIN c.courseNames cn " +
       "JOIN cn.language l " +
       "JOIN d.university univ " +
       "JOIN univ.universityNames un " +
       "JOIN un.language ul " +
       "JOIN d.departmentNames dn " +
       "JOIN dn.language dl " +
       "WHERE n.isPublic = true " +
       "AND l.code = 'EN' AND ul.code = 'EN' AND dl.code = 'EN' " +
       "AND (" +
       "LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
       "LOWER(n.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
       "LOWER(cn.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
       "LOWER(un.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
       "LOWER(dn.name) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
       ") " +
       "ORDER BY n.likes DESC, n.createdAt DESC")
    Page<NoteDTO> searchNotes(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, " +
       "n.filename, n.isPublic, cn.name, un.name, dn.name, " +
       "n.likes, u.username, u.profileImageUrl, n.createdAt) " +
       "FROM Note n " +
       "JOIN n.course c " +
       "JOIN c.department d " +
       "JOIN n.user u " +
       "JOIN c.courseNames cn " +
       "JOIN cn.language l " +
       "JOIN d.university univ " +
       "JOIN univ.universityNames un " +
       "JOIN un.language ul " +
       "JOIN d.departmentNames dn " +
       "JOIN dn.language dl " +
       "WHERE n.user.id = :userId " +
       "AND l.code = 'EN' AND ul.code = 'EN' AND dl.code = 'EN' " +
       "AND (" +
       "LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
       "LOWER(n.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
       "LOWER(cn.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
       "LOWER(un.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
       "LOWER(dn.name) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
       ") " +
       "ORDER BY n.likes DESC, n.createdAt DESC")
    Page<NoteDTO> searchUserNotes(@Param("keyword") String keyword,@Param("userId") Long userId, Pageable pageable);
    






}

