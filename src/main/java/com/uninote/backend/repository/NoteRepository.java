package com.uninote.backend.repository;

import com.uninote.backend.entity.Note;
import com.uninote.backend.entity.NoteClick;
import com.uninote.backend.entity.User;
import com.uninote.backend.interfaceProjection.NoteProjection;
import com.uninote.backend.dto.NoteDTO;
import com.uninote.backend.dto.NoteSearchResult;
import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.University;
import com.uninote.backend.dto.CourseDTO;
import com.uninote.backend.dto.CourseNameDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
       "(SELECT cn.name FROM CourseName cn WHERE cn.course = c AND cn.language.code = :languageCode), " +
       "(SELECT un.name FROM UniversityName un WHERE un.university = d.university AND un.language.code = :languageCode), " +
       "(SELECT dn.name FROM DepartmentName dn WHERE dn.department = d AND dn.language.code = :languageCode), " +
       "n.likes , u.username, u.profileImageUrl, n.createdAt, tnn.typeName, n.professor, n.academicYear) " +
       "FROM Note n " +
       "JOIN n.course c " +
       "LEFT JOIN n.noteType tn " +
       "LEFT JOIN NoteTypeName tnn ON tnn.typeId = tn.id " +
       "JOIN c.department d " +
       "JOIN n.user u " +
       "WHERE n.user.id = :userId AND  n.deleted = false AND tnn.language.code = :languageCode")
    List<NoteDTO> findByUserId(Long userId, @Param("languageCode") String languageCode);

    List<Note> findByCourse(Course course);

    List<Note> findByTitle(String title);

    List<Note> findByCourse_Department_Id(Long departmentId);

    List<Note> findByCourse_Department_University_Id(Long universityId);

    List<Note> findByCourse_Semester(int semester);



    //@Query("SELECT n FROM Note n WHERE n.isPublic = true")
    //List<Note> findPublicNotes();

    @Query(value = "SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, " +
               "n.filename, n.isPublic, cn.name, un.name, dn.name, " +
               "n.likes, u.username, u.profileImageUrl, n.createdAt, tnn.typeName, n.professor, n.academicYear, u.certified, ucg.grade, n.status ) " +
               "FROM Note n " +
               "JOIN n.course c " +
               "JOIN c.department d " +
               "JOIN n.user u " +
               "LEFT JOIN UserCourseGrade ucg ON ucg.userId = u.id and ucg.courseId = c.id "+
               "JOIN CourseName cn ON cn.course = c " +
               "JOIN cn.language l " +
               "LEFT JOIN n.noteType tn "+  
               "LEFT JOIN NoteTypeName tnn ON tnn.typeId = tn.id " +
               "JOIN UniversityName un ON un.university = d.university " +
               "JOIN un.language ul " +  
               "JOIN DepartmentName dn ON dn.department = d " +
               "JOIN dn.language dl " +  
               "WHERE l.code = :language_code AND ul.code = :language_code AND dl.code = :language_code AND tnn.language.code = :language_code " +
               "AND n.isPublic = true AND n.deleted = false")
Page<NoteDTO> findPublicNotes(Pageable pageable, @Param("language_code") String languageCode);

    @Query("SELECT n FROM Note n WHERE n.isPublic = true AND n.user = :user AND n.course.department = :department")
    List<Note> findPublicNotesByUserAndDepartment(@Param("user") User user, @Param("department") Department department);

    @Query(value = "SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, " +
               "n.filename, n.isPublic, cn.name, un.name, dn.name, " +
               "n.likes, u.username, u.profileImageUrl, n.createdAt, tnn.typeName, n.professor, n.academicYear, u.certified, ucg.grade, n.status ) " +
               "FROM Note n " +
               "JOIN n.course c " +
               "JOIN c.department d " +
               "JOIN n.user u " +
               "LEFT JOIN UserCourseGrade ucg ON ucg.userId = u.id and ucg.courseId = c.id "+
               "LEFT JOIN n.noteType tn " +
               "LEFT JOIN NoteTypeName tnn ON tnn.typeId = tn.id " +
               "JOIN CourseName cn ON cn.course = c " +
               "JOIN cn.language l " +  
               "JOIN UniversityName un ON un.university = d.university " +
               "JOIN un.language ul " +  
               "JOIN DepartmentName dn ON dn.department = d " +
               "JOIN dn.language dl " +  
               "WHERE l.code = :language_code AND ul.code = :language_code AND dl.code = :language_code AND tnn.language.code = :language_code " +
               "AND n.isPublic = true AND d = :department AND n.deleted = false")
Page<NoteDTO> findPublicNotesByDepartment(@Param("department") Department department, @Param("language_code") String languageCode ,Pageable pageable);



@Query(value = "SELECT new com.uninote.backend.dto.NoteDTO(" +
               "n.id, c.id, u.id, n.title, n.description, n.pdfUrl, " +
               "n.filename, n.isPublic, cn.name, un.name, dn.name, " +
               "n.likes, u.username, u.profileImageUrl, n.createdAt, " +
               "tnn.typeName, n.professor, n.academicYear, u.certified, ucg.grade, n.status ) " +
               "FROM Note n " +
               "JOIN n.course c " +
               "JOIN c.department d " +   
               "JOIN n.user u " +
               "LEFT JOIN UserCourseGrade ucg ON ucg.userId = u.id and ucg.courseId = c.id " +
               "LEFT JOIN n.noteType tn " +
               "LEFT JOIN NoteTypeName tnn ON tnn.typeId = tn.id " +
               "JOIN CourseName cn ON cn.course = c " +
               "JOIN cn.language l " +
               "JOIN UniversityName un ON un.university = d.university " +
               "JOIN un.language ul " +
               "JOIN DepartmentName dn ON dn.department = d " +
               "JOIN dn.language dl " +
               "WHERE l.code = :language_code " +
               "AND ul.code = :language_code " +
               "AND dl.code = :language_code " +
               "AND tnn.language.code = :language_code " +
               "AND n.isPublic = true " +
               "AND n.course.department.university = :university " +
               "AND n.deleted = false")
Page<NoteDTO> findPublicNotesByUniversity(@Param("university") University university, 
                                          @Param("language_code") String languageCode, 
                                          Pageable pageable);


    @Query(value = "SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, " +
               "n.filename, n.isPublic, cn.name, un.name, dn.name, " +
               "n.likes, u.username, u.profileImageUrl, n.createdAt, tnn.typeName, n.professor, n.academicYear, u.certified, ucg.grade, n.status ) " +
               "FROM Note n " +
               "JOIN n.course c " +
               "JOIN c.department d " +
               "JOIN n.user u " +
               "LEFT JOIN UserCourseGrade ucg ON ucg.userId = u.id and ucg.courseId = c.id "+
               "LEFT JOIN n.noteType tn " +
               "LEFT JOIN NoteTypeName tnn ON tnn.typeId = tn.id " +
               "JOIN CourseName cn ON cn.course = c " +
               "JOIN cn.language l " +  
               "JOIN UniversityName un ON un.university = d.university " +
               "JOIN un.language ul " +  
               "JOIN DepartmentName dn ON dn.department = d " +
               "JOIN dn.language dl " +  
               "WHERE l.code = :language_code AND ul.code = :language_code AND dl.code = :language_code AND tnn.language.code = :language_code " +
               "AND n.isPublic = true AND n.course = :course AND n.deleted = false")
    Page<NoteDTO> findPublicNotesByCourse(@Param("course") Course course, @Param("language_code") String language_code, Pageable pageable);

    @Query(value = "SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, " +
               "n.filename, n.isPublic, cn.name, un.name, dn.name, " +
               "n.likes, u.username, u.profileImageUrl, n.createdAt, tnn.typeName, n.professor, n.academicYear, u.certified, ucg.grade, n.status ) " +
               "FROM Note n " +
               "JOIN n.course c " +
               "JOIN c.department d " +
               "JOIN n.user u " +
               "LEFT JOIN UserCourseGrade ucg ON ucg.userId = u.id and ucg.courseId = c.id "+
               "LEFT JOIN n.noteType tn " +
               "LEFT JOIN NoteTypeName tnn ON tnn.typeId = tn.id " +
               "JOIN CourseName cn ON cn.course = c " +
               "JOIN cn.language l " +  
               "JOIN UniversityName un ON un.university = d.university " +
               "JOIN un.language ul " +  
               "JOIN DepartmentName dn ON dn.department = d " +
               "JOIN dn.language dl " +  
               "WHERE l.code = :language_code AND ul.code = :language_code AND dl.code = :language_code " +
               "AND n.isPublic = true AND n.course.department.id = :departmentId AND n.course.semester = :semester AND n.deleted = false AND tnn.language.code= :language_code")
    Page<NoteDTO> findPublicNotesByDepartmentAndSemester(@Param("departmentId") Long departmentId, @Param("semester") int semester, @Param("language_code") String language_code, Pageable pageable);

    @Query("SELECT n FROM Note n WHERE n.isPublic = true AND n.user = :user AND n.course.department.university = :university")
    List<Note> findPublicNotesByUserAndUniversity(@Param("user") User user, @Param("university") University university);

    @Query("SELECT n FROM Note n WHERE n.isPublic = true AND n.user = :user AND n.course = :course")
    List<Note> findPublicNotesByUserAndCourse(@Param("user") User user, @Param("course") Course course);

    @Query("SELECT n FROM Note n WHERE n.isPublic = true AND n.user = :user AND n.course.department.id = :departmentId AND n.course.semester = :semester")
    List<Note> findPublicNotesByUserAndDepartmentAndSemester(@Param("user") User user, @Param("departmentId") Long departmentId, @Param("semester") int semester);

    @Query("SELECT COUNT(n) FROM Note n WHERE n.user.id = :userId AND n.deleted = false")
    long countByUserId(@Param("userId") Long userId);


    @Query("SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, n.filename, n.isPublic, " +
       "(SELECT cn.name FROM CourseName cn WHERE cn.course = c AND cn.language.code = :language_code), " +
       "(SELECT un.name FROM UniversityName un WHERE un.university = d.university AND un.language.code =  :language_code), " +
       "(SELECT dn.name FROM DepartmentName dn WHERE dn.department = d AND dn.language.code = :language_code), " +
       "n.likes, u.username, u.profileImageUrl, n.createdAt, tnn.typeName, n.professor, n.academicYear, u.certified, ucg.grade, n.status ) " +
       "FROM NoteSave ns " +
       "JOIN ns.note n " +
       "LEFT JOIN n.noteType tn " +
       "LEFT JOIN NoteTypeName tnn ON tnn.typeId = tn.id " +
       "JOIN n.course c " +
       "JOIN c.department d " +
       "JOIN n.user u " +
       "LEFT JOIN UserCourseGrade ucg ON ucg.userId = u.id and ucg.courseId = c.id "+
       "WHERE ns.user.id = :userId AND ns.isActive = TRUE AND n.isPublic = TRUE AND n.deleted = false AND tnn.language.code= :language_code")
   List<NoteDTO> findPublicSavedNotesByUserId(@Param("userId") Long userId, @Param("language_code") String languageCode);

   @Query(value = "SELECT n.note_id AS id, c.course_id AS courseId, u.user_id AS userId, n.title AS title, " +
               "DBMS_LOB.SUBSTR(n.description, 4000, 1) AS description, n.pdf_url AS pdfUrl, n.filename AS filename, " +
               "(SELECT cn.course_name FROM admin.course_names cn " +
               "JOIN admin.languages l ON cn.language_id = l.language_id " +
               "WHERE cn.course_id = c.course_id AND l.language_code = :language_code) AS courseName, " +
               "(SELECT un.university_name FROM admin.university_names un " +
               "JOIN admin.languages l ON un.language_id = l.language_id " +
               "WHERE un.university_id = d.university_id AND l.language_code = :language_code) AS universityName, " +
               "(SELECT dn.department_name FROM admin.department_names dn " +
               "JOIN admin.languages l ON dn.language_id = l.language_id " +
               "WHERE dn.department_id = d.department_id AND l.language_code = :language_code) AS departmentName, " +
               "n.like_count AS likes, u.username AS username, u.profile_image_url AS profileImageUrl, n.created_at AS createdAt " +
               "FROM admin.notes n " +
               "JOIN admin.courses c ON n.course_id = c.course_id " +
               "JOIN admin.departments d ON c.department_id = d.department_id " +
               "JOIN admin.users u ON n.user_id = u.user_id " +
               "WHERE n.is_public = 1 AND u.user_id = :userId AND n.deleted = 0 " +
               "ORDER BY n.like_count DESC, n.created_at DESC " +
               "FETCH FIRST :limit ROWS ONLY", nativeQuery = true)
    List<NoteProjection> findTopPublicNotesByUser(@Param("userId") Long userId, @Param("language_code") String language_code,@Param("limit") int limit);

    
    @Query("SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, " +
       "n.filename, n.isPublic, cn.name, un.name, dn.name, " +
       "n.likes, u.username, u.profileImageUrl, n.createdAt, tn.typeName, n.professor, n.academicYear, u.certified ) " +
       "FROM Note n " +
       "JOIN n.course c " +
       "JOIN c.department d " +
       "JOIN n.user u " +
       "JOIN c.courseNames cn " +
       "JOIN cn.language l " +
       "JOIN d.university univ " +
       "JOIN univ.universityNames un " +
       "LEFT JOIN n.noteType tn " +
       "JOIN un.language ul " +
       "JOIN d.departmentNames dn " +
       "JOIN dn.language dl " +
       "WHERE n.isPublic = true AND n.deleted = false " +
       "AND l.code = 'EN' AND ul.code = 'EN' AND dl.code = 'EN' " +
       "AND (" +
       "LOWER(REPLACE(REPLACE(n.title, ' ', ''), '-', '')) LIKE LOWER(REPLACE(REPLACE(CONCAT('%', :keyword, '%'), ' ', ''), '-', '')) OR " +
       "LOWER(REPLACE(REPLACE(n.description, ' ', ''), '-', '')) LIKE LOWER(REPLACE(REPLACE(CONCAT('%', :keyword, '%'), ' ', ''), '-', '')) OR " +
       "LOWER(REPLACE(REPLACE(cn.name, ' ', ''), '-', '')) LIKE LOWER(REPLACE(REPLACE(CONCAT('%', :keyword, '%'), ' ', ''), '-', '')) OR " +
       "LOWER(REPLACE(REPLACE(un.name, ' ', ''), '-', '')) LIKE LOWER(REPLACE(REPLACE(CONCAT('%', :keyword, '%'), ' ', ''), '-', '')) OR " +
       "LOWER(REPLACE(REPLACE(dn.name, ' ', ''), '-', '')) LIKE LOWER(REPLACE(REPLACE(CONCAT('%', :keyword, '%'), ' ', ''), '-', ''))" +
       ") " +
       "ORDER BY n.likes DESC, n.createdAt DESC")
    Page<NoteDTO> searchNotes(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, " +
       "n.filename, n.isPublic, cn.name, un.name, dn.name, " +
       "n.likes, u.username, u.profileImageUrl, n.createdAt, tn.typeName, n.professor, n.academicYear) " +
       "FROM Note n " +
       "JOIN n.course c " +
       "JOIN c.department d " +
       "JOIN n.user u " +
       "JOIN c.courseNames cn " +
       "JOIN cn.language l " +
       "JOIN d.university univ " +
       "LEFT JOIN n.noteType tn " +
       "JOIN univ.universityNames un " +
       "JOIN un.language ul " +
       "JOIN d.departmentNames dn " +
       "JOIN dn.language dl " +
       "WHERE n.user.id = :userId AND  n.deleted = false " +
       "AND l.code = 'EN' AND ul.code = 'EN' AND dl.code = 'EN' " +
       "AND (" +
       "LOWER(REPLACE(REPLACE(n.title, ' ', ''), '-', '')) LIKE LOWER(REPLACE(REPLACE(CONCAT('%', :keyword, '%'), ' ', ''), '-', '')) OR " +
       "LOWER(REPLACE(REPLACE(n.description, ' ', ''), '-', '')) LIKE LOWER(REPLACE(REPLACE(CONCAT('%', :keyword, '%'), ' ', ''), '-', '')) OR " +
       "LOWER(REPLACE(REPLACE(cn.name, ' ', ''), '-', '')) LIKE LOWER(REPLACE(REPLACE(CONCAT('%', :keyword, '%'), ' ', ''), '-', '')) OR " +
       "LOWER(REPLACE(REPLACE(un.name, ' ', ''), '-', '')) LIKE LOWER(REPLACE(REPLACE(CONCAT('%', :keyword, '%'), ' ', ''), '-', '')) OR " +
       "LOWER(REPLACE(REPLACE(dn.name, ' ', ''), '-', '')) LIKE LOWER(REPLACE(REPLACE(CONCAT('%', :keyword, '%'), ' ', ''), '-', ''))" +
       ") " +
       "ORDER BY n.likes DESC, n.createdAt DESC")
    Page<NoteDTO> searchUserNotes(@Param("keyword") String keyword,@Param("userId") Long userId, Pageable pageable);
    @Query(value = "SELECT n.note_id, c.course_id, u.user_id, n.title, n.description, n.pdf_url, " +
               "n.filename, n.is_public, cn.course_name, un.university_name, dn.department_name, " +
               "n.like_count, u.username, u.profile_image_url, n.created_at, n.professor, n.academic_year, tn.type_name " +
               "FROM admin.notes n " +
               "JOIN admin.courses c ON n.course_id = c.course_id " +
               "JOIN admin.departments d ON c.department_id = d.department_id " +
               "JOIN admin.users u ON n.user_id = u.user_id " +
               "LEFT JOIN admin.note_types tn ON n.type_id = tn.type_id " +
               "JOIN admin.course_names cn ON c.course_id = cn.course_id " +
               "JOIN admin.languages l ON cn.language_id = l.language_id " +
               "JOIN admin.universities univ ON d.university_id = univ.university_id " +
               "JOIN admin.university_names un ON univ.university_id = un.university_id " +
               "JOIN admin.languages ul ON un.language_id = ul.language_id " +
               "JOIN admin.department_names dn ON d.department_id = dn.department_id " +
               "JOIN admin.languages dl ON dn.language_id = dl.language_id " +
               "WHERE  n.deleted = 0 " +
               "AND l.language_code = 'EN' AND ul.language_code = 'EN' AND dl.language_code = 'EN' " +
               "AND (" +
               "UTL_MATCH.EDIT_DISTANCE(LOWER(REPLACE(REPLACE(n.title, ' ', ''), '-', '')), LOWER(REPLACE(REPLACE(:keyword, ' ', ''), '-', ''))) <= :threshold OR " +
               "UTL_MATCH.EDIT_DISTANCE(LOWER(REPLACE(REPLACE(n.description, ' ', ''), '-', '')), LOWER(REPLACE(REPLACE(:keyword, ' ', ''), '-', ''))) <= :threshold OR " +
               "UTL_MATCH.EDIT_DISTANCE(LOWER(REPLACE(REPLACE(cn.course_name, ' ', ''), '-', '')), LOWER(REPLACE(REPLACE(:keyword, ' ', ''), '-', ''))) <= :threshold OR " +
               "UTL_MATCH.EDIT_DISTANCE(LOWER(REPLACE(REPLACE(un.university_name, ' ', ''), '-', '')), LOWER(REPLACE(REPLACE(:keyword, ' ', ''), '-', ''))) <= :threshold OR " +
               "UTL_MATCH.EDIT_DISTANCE(LOWER(REPLACE(REPLACE(dn.department_name, ' ', ''), '-', '')), LOWER(REPLACE(REPLACE(:keyword, ' ', ''), '-', ''))) <= :threshold" +
               ") " +
               "ORDER BY n.like_count DESC, n.created_at DESC", 
       nativeQuery = true)
Page<Object[]> searchUserNotesSimple(@Param("keyword") String keyword, 

                                               @Param("threshold") int threshold, 
                                               Pageable pageable);
    @Query(value = "SELECT n.note_id, c.course_id, u.user_id, n.title, n.description, n.pdf_url, " +
               "n.filename, n.is_public, cn.course_name, un.university_name, dn.department_name, " +
               "n.like_count, u.username, u.profile_image_url, n.created_at, n.professor, n.academic_year, tn.type_name, u.certified " +
               "FROM admin.notes n " +
               "JOIN admin.courses c ON n.course_id = c.course_id " +
               "JOIN admin.departments d ON c.department_id = d.department_id " +
               "JOIN admin.users u ON n.user_id = u.user_id " +
               "LEFT JOIN admin.note_types tn ON n.type_id = tn.type_id " +
               "JOIN admin.course_names cn ON c.course_id = cn.course_id " +
               "JOIN admin.languages l ON cn.language_id = l.language_id " +
               "JOIN admin.universities univ ON d.university_id = univ.university_id " +
               "JOIN admin.university_names un ON univ.university_id = un.university_id " +
               "JOIN admin.languages ul ON un.language_id = ul.language_id " +
               "JOIN admin.department_names dn ON d.department_id = dn.department_id " +
               "JOIN admin.languages dl ON dn.language_id = dl.language_id " +
               "WHERE n.user_id = :userId AND n.deleted = 0 " +
               "AND l.language_code = 'EN' AND ul.language_code = 'EN' AND dl.language_code = 'EN' " +
               "AND (" +
               "UTL_MATCH.EDIT_DISTANCE(LOWER(REPLACE(REPLACE(n.title, ' ', ''), '-', '')), LOWER(REPLACE(REPLACE(:keyword, ' ', ''), '-', ''))) <= :threshold OR " +
               "UTL_MATCH.EDIT_DISTANCE(LOWER(REPLACE(REPLACE(n.description, ' ', ''), '-', '')), LOWER(REPLACE(REPLACE(:keyword, ' ', ''), '-', ''))) <= :threshold OR " +
               "UTL_MATCH.EDIT_DISTANCE(LOWER(REPLACE(REPLACE(cn.course_name, ' ', ''), '-', '')), LOWER(REPLACE(REPLACE(:keyword, ' ', ''), '-', ''))) <= :threshold OR " +
               "UTL_MATCH.EDIT_DISTANCE(LOWER(REPLACE(REPLACE(un.university_name, ' ', ''), '-', '')), LOWER(REPLACE(REPLACE(:keyword, ' ', ''), '-', ''))) <= :threshold OR " +
               "UTL_MATCH.EDIT_DISTANCE(LOWER(REPLACE(REPLACE(dn.department_name, ' ', ''), '-', '')), LOWER(REPLACE(REPLACE(:keyword, ' ', ''), '-', ''))) <= :threshold" +
               ") " +
               "ORDER BY n.like_count DESC, n.created_at DESC", 
       nativeQuery = true)
Page<Object[]> searchUserNotesWithEditDistance(@Param("keyword") String keyword, 
                                               @Param("userId") Long userId, 
                                               @Param("threshold") int threshold, 
                                               Pageable pageable);


                                               @Query(value = "WITH note_words AS (" +
                                               "SELECT n.note_id, " +
                                               "       REGEXP_SUBSTR(LOWER(REPLACE(NVL(n.title, ''), '-', '')), '[^ ]+', 1, LEVEL) AS word, " +
                                               "       LEVEL AS word_level " +
                                               "FROM admin.notes n " +
                                               "CONNECT BY PRIOR n.note_id = n.note_id " +
                                               "AND PRIOR DBMS_RANDOM.VALUE IS NOT NULL " +
                                               "AND LEVEL <= LENGTH(LOWER(REPLACE(NVL(n.title, ''), '-', ''))) " +
                                               "- LENGTH(REPLACE(LOWER(REPLACE(NVL(n.title, ''), '-', '')), ' ', '')) + 1 " +
                                               "),  course_words AS (" +
                                               "SELECT c.course_id, " +
                                               "       REGEXP_SUBSTR(LOWER(REPLACE(NVL(cn.course_name, ''), '-', '')), '[^ ]+', 1, LEVEL) AS word, " +
                                               "       LEVEL AS word_level " +
                                               "FROM admin.courses c " +
                                               "JOIN admin.course_names cn ON c.course_id = cn.course_id " +
                                               "CONNECT BY PRIOR c.course_id = c.course_id " +
                                               "AND PRIOR DBMS_RANDOM.VALUE IS NOT NULL " +
                                               "AND LEVEL <= LENGTH(LOWER(REPLACE(NVL(cn.course_name, ''), '-', ''))) " +
                                               "- LENGTH(REPLACE(LOWER(REPLACE(NVL(cn.course_name, ''), '-', '')), ' ', '')) + 1 " +
                                               "), university_words AS (" +
                                               "SELECT univ.university_id, " +
                                               "       REGEXP_SUBSTR(LOWER(REPLACE(NVL(un.university_name, ''), '-', '')), '[^ ]+', 1, LEVEL) AS word, " +
                                               "       LEVEL AS word_level " +
                                               "FROM admin.universities univ " +
                                               "JOIN admin.university_names un ON univ.university_id = un.university_id " +
                                               "CONNECT BY PRIOR univ.university_id = univ.university_id " +
                                               "AND PRIOR DBMS_RANDOM.VALUE IS NOT NULL " +
                                               "AND LEVEL <= LENGTH(LOWER(REPLACE(NVL(un.university_name, ''), '-', ''))) " +
                                               "- LENGTH(REPLACE(LOWER(REPLACE(NVL(un.university_name, ''), '-', '')), ' ', '')) + 1 " +
                                               "), department_words AS (" +
                                               "SELECT d.department_id, " +
                                               "       REGEXP_SUBSTR(LOWER(REPLACE(NVL(dn.department_name, ''), '-', '')), '[^ ]+', 1, LEVEL) AS word, " +
                                               "       LEVEL AS word_level " +
                                               "FROM admin.departments d " +
                                               "JOIN admin.department_names dn ON d.department_id = dn.department_id " +
                                               "CONNECT BY PRIOR d.department_id = d.department_id " +
                                               "AND PRIOR DBMS_RANDOM.VALUE IS NOT NULL " +
                                               "AND LEVEL <= LENGTH(LOWER(REPLACE(NVL(dn.department_name, ''), '-', ''))) " +
                                               "- LENGTH(REPLACE(LOWER(REPLACE(NVL(dn.department_name, ''), '-', '')), ' ', '')) + 1 " +
                                               ") " +
                                               "SELECT * FROM (" +
                                               "SELECT n.note_id AS id, " +
                                               "       c.course_id AS courseId, " +
                                               "       u.user_id AS userId, " +
                                               "       n.title AS title, " +
                                               "       DBMS_LOB.SUBSTR(n.description, 4000, 1) AS description, " +
                                               "       n.pdf_url AS pdfUrl, " +
                                               "       n.filename AS filename, " +
                                               "       n.is_public, " +
                                               "       cn.course_name AS courseName, " +
                                               "       un.university_name AS universityName, " +
                                               "       dn.department_name AS departmentName, " +
                                               "       n.like_count AS likes, " +
                                               "       u.username AS username, " +
                                               "       u.profile_image_url AS profileImageUrl, " +
                                               "       n.created_at AS createdAt, " +
                                               "       n.professor, " +
                                               "       n.academic_year, " +
                                               "       tn.type_name, " +
                                               "(CASE WHEN nw.word IS NOT NULL THEN 5 ELSE 0 END + " +   
                                               " CASE WHEN cw.word IS NOT NULL THEN 2 ELSE 0 END + " +
                                               " CASE WHEN uw.word IS NOT NULL THEN 1 ELSE 0 END + " +
                                               " CASE WHEN depw.word IS NOT NULL THEN 1 ELSE 0 END) AS relevance_score, " +
                                               "ROW_NUMBER() OVER (ORDER BY "  +
                                               "(CASE WHEN nw.word IS NOT NULL THEN 5 ELSE 0 END + " +
                                               " CASE WHEN cw.word IS NOT NULL THEN 2 ELSE 0 END + " +
                                               " CASE WHEN uw.word IS NOT NULL THEN 1 ELSE 0 END + " +
                                               " CASE WHEN depw.word IS NOT NULL THEN 1 ELSE 0 END) " +
                                               "DESC) AS row_number " +
                                               "FROM admin.notes n " +
                                               "JOIN admin.courses c ON n.course_id = c.course_id " +
                                               "JOIN admin.departments d ON c.department_id = d.department_id " +
                                               "JOIN admin.users u ON n.user_id = u.user_id " +
                                               "LEFT JOIN admin.note_types tn ON n.type_id = tn.type_id " +
                                               "JOIN admin.course_names cn ON c.course_id = cn.course_id " +
                                               "JOIN admin.languages l1 ON cn.language_id = l1.language_id AND l1.language_code = 'EN' " +
                                               "JOIN admin.universities univ ON d.university_id = univ.university_id " +
                                               "JOIN admin.university_names un ON univ.university_id = un.university_id " +
                                               "JOIN admin.languages l2 ON un.language_id = l2.language_id AND l2.language_code = 'EN' " +
                                               "JOIN admin.department_names dn ON d.department_id = dn.department_id " +
                                               "JOIN admin.languages l3 ON dn.language_id = l3.language_id AND l3.language_code = 'EN' " +
                                               "LEFT JOIN note_words nw ON nw.note_id = n.note_id AND UTL_MATCH.EDIT_DISTANCE(nw.word, LOWER(REPLACE(:keyword, ' ', ''))) <= CEIL(:threshold * length(nw.word)) " +
                                               "LEFT JOIN course_words cw ON cw.course_id = c.course_id AND UTL_MATCH.EDIT_DISTANCE(cw.word, LOWER(REPLACE(:keyword, ' ', ''))) <= CEIL(:threshold * length(cw.word)) " +
                                               "LEFT JOIN university_words uw ON uw.university_id = univ.university_id AND UTL_MATCH.EDIT_DISTANCE(uw.word, LOWER(REPLACE(:keyword, ' ', ''))) <= CEIL(:threshold * length(uw.word)) " +
                                               "LEFT JOIN department_words depw ON depw.department_id = d.department_id AND UTL_MATCH.EDIT_DISTANCE(depw.word, LOWER(REPLACE(:keyword, ' ', ''))) <= CEIL(:threshold * length(depw.word)) " +
                                               "JOIN user_course_grades ucg ON ucg.user_id = u.user_id and ucg.course_id = c.course_id "+
                                               "WHERE n.is_public = 1 AND n.deleted = 0 " +
                                               "AND (nw.word IS NOT NULL OR cw.word IS NOT NULL OR uw.word IS NOT NULL OR depw.word IS NOT NULL)) " +
                                               "WHERE row_number BETWEEN :start_row AND :end_row " +
                                               "ORDER BY relevance_score DESC", 
                                       nativeQuery = true)
                                List<Object[]> searchNotes(
                                        @Param("keyword") String keyword, 
                                        @Param("threshold") float threshold, 
                                        @Param("start_row") int startRow, 
                                        @Param("end_row") int endRow);



                                        @Query(value = "WITH note_words AS (" +
                   "    SELECT n.note_id, " +
                   "           REGEXP_SUBSTR(LOWER(REPLACE(NVL(n.title, ''), '-', '')), '[^ ]+', 1, LEVEL) AS word, " +
                   "           LEVEL AS word_level " +
                   "    FROM admin.notes n " +
                   "    CONNECT BY PRIOR n.note_id = n.note_id " +
                   "    AND PRIOR DBMS_RANDOM.VALUE IS NOT NULL " +
                   "    AND LEVEL <= LENGTH(LOWER(REPLACE(NVL(n.title, ''), '-', ''))) " +
                   "    - LENGTH(REPLACE(LOWER(REPLACE(NVL(n.title, ''), '-', '')), ' ', '')) + 1 " +
                   "), " +
                   "course_words AS (" +
                   "    SELECT c.course_id, " +
                   "           REGEXP_SUBSTR(LOWER(REPLACE(NVL(cn.course_name, ''), '-', '')), '[^ ]+', 1, LEVEL) AS word, " +
                   "           LEVEL AS word_level " +
                   "    FROM admin.courses c " +
                   "    JOIN admin.course_names cn ON c.course_id = cn.course_id " +
                   "    CONNECT BY PRIOR c.course_id = c.course_id " +
                   "    AND PRIOR DBMS_RANDOM.VALUE IS NOT NULL " +
                   "    AND LEVEL <= LENGTH(LOWER(REPLACE(NVL(cn.course_name, ''), '-', ''))) " +
                   "    - LENGTH(REPLACE(LOWER(REPLACE(NVL(cn.course_name, ''), '-', '')), ' ', '')) + 1 " +
                   "), " +
                   "university_words AS (" +
                   "    SELECT univ.university_id, " +
                   "           REGEXP_SUBSTR(LOWER(REPLACE(NVL(un.university_name, ''), '-', '')), '[^ ]+', 1, LEVEL) AS word, " +
                   "           LEVEL AS word_level " +
                   "    FROM admin.universities univ " +
                   "    JOIN admin.university_names un ON univ.university_id = un.university_id " +
                   "    CONNECT BY PRIOR univ.university_id = univ.university_id " +
                   "    AND PRIOR DBMS_RANDOM.VALUE IS NOT NULL " +
                   "    AND LEVEL <= LENGTH(LOWER(REPLACE(NVL(un.university_name, ''), '-', ''))) " +
                   "    - LENGTH(REPLACE(LOWER(REPLACE(NVL(un.university_name, ''), '-', '')), ' ', '')) + 1 " +
                   "), " +
                   "department_words AS (" +
                   "    SELECT d.department_id, " +
                   "           REGEXP_SUBSTR(LOWER(REPLACE(NVL(dn.department_name, ''), '-', '')), '[^ ]+', 1, LEVEL) AS word, " +
                   "           LEVEL AS word_level " +
                   "    FROM admin.departments d " +
                   "    JOIN admin.department_names dn ON d.department_id = dn.department_id " +
                   "    CONNECT BY PRIOR d.department_id = d.department_id " +
                   "    AND PRIOR DBMS_RANDOM.VALUE IS NOT NULL " +
                   "    AND LEVEL <= LENGTH(LOWER(REPLACE(NVL(dn.department_name, ''), '-', ''))) " +
                   "    - LENGTH(REPLACE(LOWER(REPLACE(NVL(dn.department_name, ''), '-', '')), ' ', '')) + 1 " +
                   "), " +
                   "total_elements AS (" +
                   "    SELECT COUNT(*) AS total_count " +
                   "    FROM admin.notes n " +
                   "    JOIN admin.courses c ON n.course_id = c.course_id " +
                   "    JOIN admin.departments d ON c.department_id = d.department_id " +
                   "    JOIN admin.users u ON n.user_id = u.user_id " +
                   "    JOIN admin.course_names cn ON c.course_id = cn.course_id " +
                   "    JOIN admin.languages l1 ON cn.language_id = l1.language_id AND l1.language_code = 'EN' " +
                   "    JOIN admin.universities univ ON d.university_id = univ.university_id " +
                   "    JOIN admin.university_names un ON univ.university_id = un.university_id " +
                   "    JOIN admin.languages l2 ON un.language_id = l2.language_id AND l2.language_code = 'EN' " +
                   "    JOIN admin.department_names dn ON d.department_id = dn.department_id " +
                   "    JOIN admin.languages l3 ON dn.language_id = l3.language_id AND l3.language_code = 'EN' " +
                   "    LEFT JOIN note_words nw ON nw.note_id = n.note_id AND UTL_MATCH.EDIT_DISTANCE(nw.word, LOWER(REPLACE(:keyword, ' ', ''))) <= CEIL(:threshold * length(nw.word)) " +
                   "    LEFT JOIN course_words cw ON cw.course_id = c.course_id AND UTL_MATCH.EDIT_DISTANCE(cw.word, LOWER(REPLACE(:keyword, ' ', ''))) <= CEIL(:threshold * length(cw.word)) " +
                   "    LEFT JOIN university_words uw ON uw.university_id = univ.university_id AND UTL_MATCH.EDIT_DISTANCE(uw.word, LOWER(REPLACE(:keyword, ' ', ''))) <= CEIL(:threshold * length(uw.word)) " +
                   "    LEFT JOIN department_words depw ON depw.department_id = d.department_id AND UTL_MATCH.EDIT_DISTANCE(depw.word, LOWER(REPLACE(:keyword, ' ', ''))) <= CEIL(:threshold * length(depw.word)) " +
                   "    WHERE n.is_public = 1 AND n.deleted = 0 " +
                   "    AND (nw.word IS NOT NULL OR cw.word IS NOT NULL OR uw.word IS NOT NULL OR depw.word IS NOT NULL) " +
                   ") " +
                   "SELECT id, courseId, userId, title, description, pdfUrl, filename, is_public, courseName, universityName, departmentName, likes, username, profileImageUrl, createdAt, professor, academic_year, type_name, certified,  grade , relevance_score, row_number, " +
                   "       total_elements.total_count AS total_elements, " +
                   "       CEIL(total_elements.total_count / (:end_row - :start_row + 1)) AS total_pages " +
                   "FROM ( " +
                   "    SELECT n.note_id AS id, " +
                   "           c.course_id AS courseId, " +
                   "           u.user_id AS userId, " +
                   "           n.title AS title, " +
                   "           DBMS_LOB.SUBSTR(n.description, 4000, 1) AS description, " +
                   "           n.pdf_url AS pdfUrl, " +
                   "           n.filename AS filename, " +
                   "           n.is_public, " +
                   "           cn.course_name AS courseName, " +  
                   "           un.university_name AS universityName, " +
                   "           dn.department_name AS departmentName, " +
                   "           n.like_count AS likes, " +
                   "           u.username AS username, " +
                   "           u.profile_image_url AS profileImageUrl, " +
                   "           n.created_at AS createdAt, " +
                   "           n.professor, " +
                   "           n.academic_year, " +
                   "           tn.type_name, " +
                   "            u.certified, "+
                   "            ucg.grade AS grade , "+
                   "           (CASE WHEN nw.word IS NOT NULL THEN 5 ELSE 0 END + " +
                   "            CASE WHEN cw.word IS NOT NULL THEN 2 ELSE 0 END + " +
                   "            CASE WHEN uw.word IS NOT NULL THEN 1 ELSE 0 END + " +
                   "            CASE WHEN depw.word IS NOT NULL THEN 1 ELSE 0 END) AS relevance_score, " +
                   "           ROW_NUMBER() OVER (ORDER BY (CASE WHEN nw.word IS NOT NULL THEN 5 ELSE 0 END + " +
                   "                                         CASE WHEN cw.word IS NOT NULL THEN 2 ELSE 0 END + " +
                   "                                         CASE WHEN uw.word IS NOT NULL THEN 1 ELSE 0 END + " +
                   "                                         CASE WHEN depw.word IS NOT NULL THEN 1 ELSE 0 END) DESC) AS row_number " +
                   "    FROM admin.notes n " +
                   "    JOIN admin.courses c ON n.course_id = c.course_id " +
                   "    JOIN admin.departments d ON c.department_id = d.department_id " +
                   "    JOIN admin.users u ON n.user_id = u.user_id " +
                   "    LEFT JOIN admin.note_types tn ON n.type_id = tn.type_id " +
                   "    JOIN admin.course_names cn ON c.course_id = cn.course_id " +
                   "    JOIN admin.languages l1 ON cn.language_id = l1.language_id AND l1.language_code = :language_code " +
                   "    JOIN admin.universities univ ON d.university_id = univ.university_id " +
                   "    JOIN admin.university_names un ON univ.university_id = un.university_id " +
                   "    JOIN admin.languages l2 ON un.language_id = l2.language_id AND l2.language_code = :language_code " +
                   "    JOIN admin.department_names dn ON d.department_id = dn.department_id " +
                   "    JOIN admin.languages l3 ON dn.language_id = l3.language_id AND l3.language_code = :language_code " +
                   "    LEFT JOIN note_words nw ON nw.note_id = n.note_id AND UTL_MATCH.EDIT_DISTANCE(nw.word, LOWER(REPLACE(:keyword, ' ', ''))) <= CEIL(:threshold * length(nw.word)) " +
                   "    LEFT JOIN course_words cw ON cw.course_id = c.course_id AND UTL_MATCH.EDIT_DISTANCE(cw.word, LOWER(REPLACE(:keyword, ' ', ''))) <= CEIL(:threshold * length(cw.word)) " +
                   "    LEFT JOIN university_words uw ON uw.university_id = univ.university_id AND UTL_MATCH.EDIT_DISTANCE(uw.word, LOWER(REPLACE(:keyword, ' ', ''))) <= CEIL(:threshold * length(uw.word)) " +
                   "    LEFT JOIN department_words depw ON depw.department_id = d.department_id AND UTL_MATCH.EDIT_DISTANCE(depw.word, LOWER(REPLACE(:keyword, ' ', ''))) <= CEIL(:threshold * length(depw.word)) " +
                   "    LEFT JOIN admin.user_course_grades ucg ON ucg.user_id = n.user_id and ucg.course_id = n.course_id "+
                   "    WHERE n.is_public = 1 AND n.deleted = 0 " +
                   "    AND (nw.word IS NOT NULL OR cw.word IS NOT NULL OR uw.word IS NOT NULL OR depw.word IS NOT NULL) " +
                   ") result, total_elements " +
                   "WHERE row_number BETWEEN :start_row AND :end_row " +
                   "ORDER BY relevance_score DESC", 
           nativeQuery = true)
    List<Object[]> searchNotesWithPagination(
            @Param("keyword") String keyword, 
            @Param("threshold") float threshold, 
            @Param("start_row") int startRow, 
            @Param("end_row") int endRow,
            @Param("language_code") String languageCode);
                                
                                

 /* "AND (" +
                                                // Exact Match with LIKE
                                                "    LOWER(n.title) LIKE '%' || LOWER(:keyword) || '%' OR " +
                                                "    LOWER(DBMS_LOB.SUBSTR(NVL(n.description,''), 4000, 1)) LIKE '%' || LOWER(:keyword) || '%' OR " +
                                                "    LOWER(cn.course_name) LIKE '%' || LOWER(:keyword) || '%' OR " +
                                                "    LOWER(un.university_name) LIKE '%' || LOWER(:keyword) || '%' OR " +
                                                "    LOWER(dn.department_name) LIKE '%' || LOWER(:keyword) || '%' OR " +
                                                // Fuzzy Match with EDIT_DISTANCE using optimized thresholds
                                                 "    (LENGTH(:keyword) > 3 AND " +  // Apply EDIT_DISTANCE only for longer keywords
                                                "    (UTL_MATCH.EDIT_DISTANCE(LOWER(REPLACE(REPLACE(NVL(n.title, ''), ' ', ''), '-', '')), LOWER(REPLACE(REPLACE(:keyword, ' ', ''), '-', ''))) <= :threshold OR " +
                                                "     UTL_MATCH.EDIT_DISTANCE(LOWER(REPLACE(REPLACE(NVL(DBMS_LOB.SUBSTR(NVL(n.description,''), 4000, 1), ''), ' ', ''), '-', '')), LOWER(REPLACE(REPLACE(:keyword, ' ', ''), '-', ''))) <= :threshold OR " +
                                                "     UTL_MATCH.EDIT_DISTANCE(LOWER(REPLACE(REPLACE(NVL(cn.course_name, ''), ' ', ''), '-', '')), LOWER(REPLACE(REPLACE(:keyword, ' ', ''), '-', ''))) <= :threshold OR " +
                                                "     UTL_MATCH.EDIT_DISTANCE(LOWER(REPLACE(REPLACE(NVL(un.university_name, ''), ' ', ''), '-', '')), LOWER(REPLACE(REPLACE(:keyword, ' ', ''), '-', ''))) <= :threshold OR " +
                                                "     UTL_MATCH.EDIT_DISTANCE(LOWER(REPLACE(REPLACE(NVL(dn.department_name, ''), ' ', ''), '-', '')), LOWER(REPLACE(REPLACE(:keyword, ' ', ''), '-', ''))) <= :threshold))" +
                                                ") " +*/
    @Query(value = "SELECT n.note_id AS id, c.course_id AS courseId, u.user_id AS userId, n.title AS title, " +
               "DBMS_LOB.SUBSTR(n.description, 4000, 1) AS description, n.pdf_url AS pdfUrl, n.filename AS filename, " +
               "(SELECT cn.course_name FROM admin.course_names cn " +
               "JOIN admin.languages l ON cn.language_id = l.language_id " +
               "WHERE cn.course_id = c.course_id AND l.language_code = 'EN') AS courseName, " +
               "(SELECT un.university_name FROM admin.university_names un " +
               "JOIN admin.languages l ON un.language_id = l.language_id " +
               "WHERE un.university_id = d.university_id AND l.language_code = 'EN') AS universityName, " +
               "(SELECT dn.department_name FROM admin.department_names dn " +
               "JOIN admin.languages l ON dn.language_id = l.language_id " +
               "WHERE dn.department_id = d.department_id AND l.language_code = 'EN') AS departmentName, " +
               "n.like_count AS likes, u.username AS username, u.profile_image_url AS profileImageUrl, n.created_at AS createdAt " +
               "FROM admin.notes n " +
               "JOIN admin.note_collection_items ci ON n.note_id = ci.note_id " +  
               "JOIN admin.courses c ON n.course_id = c.course_id " +
               "JOIN admin.departments d ON c.department_id = d.department_id " +
               "JOIN admin.users u ON n.user_id = u.user_id " +
               "WHERE ci.collection_id = :collectionId " +  
               "ORDER BY n.created_at ASC " +  
               "FETCH FIRST 1 ROWS ONLY", nativeQuery = true)
   NoteDTO findFirstNoteByCollectionId(@Param("collectionId") Long collectionId);


   long countByUserIdAndIsPublic(Long userId, boolean isPublic);


   @Modifying
   @Query("UPDATE Note n SET n.deleted = true WHERE n.user.id = :userId")
   void softDeleteByUserId(@Param("userId") Long userId);




   @Query("SELECT n.id FROM Note n WHERE n.uuid = :uuid")
    Optional<Long> findIdByUuid(@Param("uuid") String uuid);

    
    @Query("SELECT n.uuid FROM Note n WHERE n.id = :id")
    Optional<String> findUuidById(@Param("id") Long id);

      @Query(value = "SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, " +
               "n.filename, n.isPublic, cn.name, un.name, dn.name, " +
               "n.likes, u.username, u.profileImageUrl, n.createdAt     ) " +
               "FROM Note n " +
               "JOIN n.course c " +
               "JOIN c.department d " +
               "JOIN n.user u " +
               "JOIN CourseName cn ON cn.course = c " +
               "JOIN cn.language l " +  
               "LEFT JOIN n.noteType tn " +
               "JOIN UniversityName un ON un.university = d.university " +
               "JOIN un.language ul " +  
               "JOIN DepartmentName dn ON dn.department = d " +
               "JOIN dn.language dl " +  
               "WHERE l.code = :language_code AND ul.code = :language_code AND dl.code = :language_code " +
               "AND n.isPublic = true AND d = :department AND n   .deleted = false ORDER BY n.createdAt desc")
   Page<NoteDTO> findRecentPublicNotesByDepartment(@Param("department") Department department, @Param("language_code") String languageCode, Pageable pageable);

   @Query(value = "SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, " +
               "n.filename, n.isPublic, cn.name, un.name, dn.name, " +
               "n.likes, u.username, u.profileImageUrl, n.createdAt, tnn.typeName, n.professor, n.academicYear, u.certified, ucg.grade, n.status ) " +
               "FROM Note n " +
               "JOIN n.course c " +
               "JOIN c.department d " +
               "JOIN n.user u " +
               "LEFT JOIN UserCourseGrade ucg ON ucg.userId = u.id and ucg.courseId = c.id "+
               "JOIN CourseName cn ON cn.course = c " +
               "JOIN cn.language l " +  
               "LEFT JOIN n.noteType tn " +
               "LEFT JOIN NoteTypeName tnn ON tnn.typeId = tn.id " +
               "JOIN tnn.language tl " +
               "JOIN UniversityName un ON un.university = d.university " +
               "JOIN un.language ul " +  
               "JOIN DepartmentName dn ON dn.department = d " +
               "JOIN dn.language dl " +  
               "WHERE l.code = :language_code AND ul.code = :language_code AND dl.code = :language_code AND tl.code= :language_code " +
               "AND n.noteType.typeId = :typeId " +
               "AND n.isPublic = true AND d = :department AND n.deleted = false ")
   Page<NoteDTO> findPublicNotesByDepartmentByType(@Param("department") Department department,@Param("typeId") Long typeId, @Param("language_code") String languageCode ,Pageable pageable);


   
   @Query(value = "SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, " +
               "n.filename, n.isPublic, cn.name, un.name, dn.name, " +
               "n.likes, u.username, u.profileImageUrl, n.createdAt, tnn.typeName, n.professor, n.academicYear, u.certified, ucg.grade, n.status ) " +
               "FROM Note n " +
               "JOIN n.course c " +
               "JOIN c.department d " +
               "JOIN n.user u " +
               "LEFT JOIN UserCourseGrade ucg ON ucg.userId = u.id and ucg.courseId = c.id "+
               "LEFT JOIN n.noteType tn " +
               "LEFT JOIN NoteTypeName tnn ON tnn.typeId = tn.id " +
               "JOIN tnn.language tl " +
               "JOIN CourseName cn ON cn.course = c " +
               "JOIN cn.language l " +  
               "JOIN UniversityName un ON un.university = d.university " +
               "JOIN un.language ul " +  
               "JOIN DepartmentName dn ON dn.department = d " +
               "JOIN dn.language dl " +  
               "WHERE l.code = :language_code AND ul.code = :language_code AND dl.code = :language_code AND tl.code = :language_code " + 
               "AND n.noteType.typeId = :typeId " +
               "AND n.isPublic = true AND n.course.department.university = :university AND  n.deleted = false")
   Page<NoteDTO> findPublicNotesByUniversityByType(@Param("university") University university, @Param("typeId") Long typeId,@Param("language_code") String languageCode,Pageable pageable);

    @Query(value = "SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, " +
               "n.filename, n.isPublic, cn.name, un.name, dn.name, " +
               "n.likes, u.username, u.profileImageUrl, n.createdAt, tnn.typeName, n.professor, n.academicYear, u.certified, ucg.grade, n.status) " +
               "FROM Note n " +
               "JOIN n.course c " +
               "JOIN c.department d " +
               "JOIN n.user u " +
               "LEFT JOIN UserCourseGrade ucg ON ucg.userId = u.id and ucg.courseId = c.id "+
               "LEFT JOIN n.noteType tn " +
               "LEFT JOIN NoteTypeName tnn ON tnn.typeId = tn.id " +
               "JOIN tnn.language tl " +
               "JOIN CourseName cn ON cn.course = c " +
               "JOIN cn.language l " +  
               "JOIN UniversityName un ON un.university = d.university " +
               "JOIN un.language ul " +  
               "JOIN DepartmentName dn ON dn.department = d " +
               "JOIN dn.language dl " +  
               "WHERE l.code = :language_code AND ul.code = :language_code AND dl.code = :language_code AND tl.code = :language_code " +
               "AND n.noteType.typeId = :typeId " +
               "AND n.isPublic = true AND n.course = :course AND n.deleted = false")
    Page<NoteDTO> findPublicNotesByCourseByType(@Param("course") Course course, @Param("typeId") Long typeId, @Param("language_code") String languageCode,Pageable pageable);

    @Query(value = "SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, " +
    "n.filename, n.isPublic, cn.name, un.name, dn.name, " +
    "n.likes, u.username, u.profileImageUrl, n.createdAt, tnn.typeName, n.professor, n.academicYear, u.certified, ucg.grade, n.status  ) " +
    "FROM Note n " +
    "JOIN n.course c " +
    "JOIN c.department d " +
    "JOIN n.user u " +
    "LEFT JOIN UserCourseGrade ucg ON ucg.userId = u.id and ucg.courseId = c.id "+
    "LEFT JOIN n.noteType tn " +
    "LEFT JOIN NoteTypeName tnn ON tnn.typeId = tn.id " +
    "JOIN tnn.language tl " +
    "JOIN CourseName cn ON cn.course = c " +
    "JOIN cn.language l " +  
    "JOIN UniversityName un ON un.university = d.university " +
    "JOIN un.language ul " +  
    "JOIN DepartmentName dn ON dn.department = d " +
    "JOIN dn.language dl " +  
    "WHERE l.code = :language_code AND ul.code = :language_code AND dl.code = :language_code AND tl.code = :language_code " +
    "AND n.noteType.typeId = :typeId " +
    "AND n.isPublic = true AND n.course.department.id = :departmentId AND n.course.semester = :semester AND n.deleted = false")
    Page<NoteDTO> findPublicNotesByDepartmentAndSemesterByType(@Param("departmentId") Long departmentId, @Param("semester") int semester, @Param("typeId") Long typeId, @Param("language_code") String languageCode,Pageable pageable);


    
    @Query(value = "SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, " +
    "n.filename, n.isPublic, cn.name, un.name, dn.name, " +
    "n.likes, u.username, u.profileImageUrl, n.createdAt, tnn.typeName, n.professor, n.academicYear, u.certified, ucg.grade, n.status  ) " +
    "FROM Note n " +
    "JOIN n.course c " +
    "JOIN c.department d " +
    "JOIN n.user u " +
    "LEFT JOIN UserCourseGrade ucg ON ucg.userId = u.id AND ucg.courseId = c.id " +
    "LEFT JOIN n.noteType tn " +
    "LEFT JOIN NoteTypeName tnn ON tnn.typeId = tn.id " +
    "JOIN tnn.language tl " +
    "JOIN CourseName cn ON cn.course = c " +
    "JOIN cn.language l " +
    "JOIN UniversityName un ON un.university = d.university " +
    "JOIN un.language ul " +
    "JOIN DepartmentName dn ON dn.department = d " +
    "JOIN dn.language dl " +
    "WHERE l.code = :language_code AND ul.code = :language_code AND dl.code = :language_code AND tl.code = :language_code " +
    "AND n.noteType.typeId = :typeId " +
    "AND n.isPublic = true AND n.deleted = false")
Page<NoteDTO> findPublicNotesByType(Pageable pageable, @Param("typeId") Long typeId, @Param("language_code") String languageCode);



   @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END FROM admin.NOTES WHERE COURSE_ID = :courseId", nativeQuery = true)
   int existsByCourseId(@Param("courseId") Long courseId);



   @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END " +
   "FROM admin.NOTES n " +
   "JOIN admin.COURSES c ON n.COURSE_ID = c.COURSE_ID " +
   "JOIN admin.DEPARTMENTS d ON c.DEPARTMENT_ID = d.DEPARTMENT_ID " +
   "WHERE d.DEPARTMENT_ID = :departmentId", 
nativeQuery = true)
int existsByDepartmentId(@Param("departmentId") Long departmentId);


@Query("SELECT DISTINCT new com.uninote.backend.dto.CourseNameDTO(c.id, cn.name, 'EN') " +
       "FROM Note n " +
       "JOIN n.course c " +
       "JOIN c.courseNames cn " +
       "WHERE n.user.id = :userId AND "+
       "n.deleted = false")
List<CourseNameDTO> findCoursesWithNotesByUserId(@Param("userId") Long userId);




@Query("SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, n.filename, n.isPublic, " +
       "(SELECT cn.name FROM CourseName cn WHERE cn.course = c AND cn.language.code = 'EN'), " +
       "(SELECT un.name FROM UniversityName un WHERE un.university = d.university AND un.language.code = 'EN'), " +
       "(SELECT dn.name FROM DepartmentName dn WHERE dn.department = d AND dn.language.code = 'EN'), " +
       "n.likes, u.username, u.profileImageUrl, n.createdAt, tn.typeName, n.professor, n.academicYear, u.certified, ucg.grade, n.status  ) " +
       "FROM Note n " +
       "JOIN n.course c " +
       "JOIN c.department d " +
       "JOIN n.user u " +
       "LEFT JOIN n.noteType tn " +
       "LEFT JOIN UserCourseGrade ucg ON ucg.userId = u.id AND ucg.courseId = c.id " +
       "WHERE n.user.id IN (SELECT n2.user.id FROM Note n2 GROUP BY n2.user.id HAVING COUNT(n2.id) > 3) " +
       "AND n.user.id IN (SELECT nv.note.user.id FROM NoteView nv GROUP BY nv.note.user.id " +
       "HAVING COUNT(nv) >= :minCreatorViews) " +
       "AND n.likes > :minLikes " +
       "AND n.isPublic = TRUE " +
       "AND n.deleted = FALSE")
List<NoteDTO> findNotesByGoodCreators(@Param("minLikes") long minLikes, @Param("minCreatorViews") long minCreatorViews);




      @Query("SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, n.filename, n.isPublic, " +
         "(SELECT cn.name FROM CourseName cn WHERE cn.course = c AND cn.language.code = 'EN'), " +
         "(SELECT un.name FROM UniversityName un WHERE un.university = d.university AND un.language.code = 'EN'), " +
         "(SELECT dn.name FROM DepartmentName dn WHERE dn.department = d AND dn.language.code = 'EN'), " +
         "n.likes, u.username, u.profileImageUrl, n.createdAt, tn.typeName, n.professor, n.academicYear, u.certified, ucg.grade, n.status ) " +
         "FROM Note n " +
         "JOIN n.course c " +
         "JOIN c.department d " +
         "JOIN n.user u " +
         "LEFT JOIN n.noteType tn " +
         "LEFT JOIN UserCourseGrade ucg ON ucg.userId = u.id AND ucg.courseId = c.id " +
         "WHERE n.createdAt > :recentThreshold " +
         "AND n.isPublic = TRUE AND n.deleted = FALSE")
   List<NoteDTO> findRecentNotes(@Param("recentThreshold") LocalDateTime recentThreshold);

   @Query(value = "SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, " +
               "n.filename, n.isPublic, cn.name, un.name, dn.name, " +
               "n.likes, u.username, u.profileImageUrl, n.createdAt, tnn.typeName, n.professor, n.academicYear, u.certified, ucg.grade, n.status ) " +
               "FROM Note n " +
               "JOIN n.course c " +
               "JOIN c.department d " +
               "JOIN n.user u " +
               "LEFT JOIN UserCourseGrade ucg ON ucg.userId = u.id and ucg.courseId = c.id " +
               "LEFT JOIN n.noteType tn " +
               "LEFT JOIN NoteTypeName tnn ON tnn.typeId = tn.id " +
               "JOIN CourseName cn ON cn.course = c " +
               "JOIN cn.language lang " + 
               "JOIN UniversityName un ON un.university = d.university " +
               "JOIN un.language ul " +
               "JOIN DepartmentName dn ON dn.department = d " +
               "JOIN dn.language dl " +
               "WHERE  ul.code = :language_code AND dl.code = :language_code AND tnn.language.code = :language_code AND lang.code = :language_code " +
               "AND n.id IN :noteIds AND n.isPublic = true AND n.deleted = false")
   List<NoteDTO> findNotesByIds(@Param("noteIds") List<Long> noteIds, @Param("language_code") String languageCode);




   @Query("SELECT n.id FROM Note n where n.deleted = false and n.isPublic = true")
   List<Long> findNonDeletedNoteIds();

// openconf

   @Query("SELECT n.pdfUrl FROM Note n where n.id = :noteId")
   String findPdfUrl(@Param("noteId") Long noteId);

   @Query(value = "SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, " +
               "n.filename, n.isPublic, cn.name, un.name, dn.name, " +
               "n.likes, u.username, u.profileImageUrl, n.createdAt, tnn.typeName, n.professor, n.academicYear, u.certified, ucg.grade, n.status ) " +
               "FROM Note n " +
               "JOIN n.course c " +
               "JOIN c.department d " +
               "JOIN n.user u " +
               "LEFT JOIN UserCourseGrade ucg ON ucg.userId = u.id and ucg.courseId = c.id " +
               "LEFT JOIN n.noteType tn " +
               "LEFT JOIN NoteTypeName tnn ON tnn.typeId = tn.id " +
               "JOIN CourseName cn ON cn.course = c " +
               "JOIN cn.language lang " + 
               "JOIN UniversityName un ON un.university = d.university " +
               "JOIN un.language ul " +
               "JOIN DepartmentName dn ON dn.department = d " +
               "JOIN dn.language dl " +
               "WHERE  ul.code = :language_code AND dl.code = :language_code AND tnn.language.code = :language_code " +
               "AND n.id = :noteId AND n.isPublic = true AND n.deleted = false")
   Optional<NoteDTO> getNoteDataById(@Param("noteId") Long noteId, @Param("language_code") String languageCode);


   boolean existsBySlugTitle(String slug);



   @Query("SELECT n FROM Note n WHERE n.status = :status and deleted = False ")
   List<Note> findByStatus(String status);



   @Query(
    "SELECT new com.uninote.backend.dto.NoteDTO(" +
    "    n.id, c.id, u.id, n.title, n.description, n.pdfUrl, n.filename, " +
    "    n.isPublic, cn.name, un.name, dn.name, " +
    "    n.likes, u.username, u.profileImageUrl, n.createdAt, tnn.typeName, " +
    "    n.professor, n.academicYear, u.certified, ucg.grade, n.status, " +
    "    (SELECT COUNT(nl) FROM NoteLike nl WHERE nl.note.id = n.id AND nl.isActive = true), " +
    "    (SELECT COUNT(nv) FROM NoteView nv WHERE nv.note.id = n.id)" +
    ") " +
    "FROM Note n " +
    "JOIN n.course c " +
    "JOIN c.department d " +
    "JOIN n.user u " +
    "LEFT JOIN UserCourseGrade ucg ON ucg.userId = u.id AND ucg.courseId = c.id " +
    "LEFT JOIN n.noteType tn " +
    "LEFT JOIN NoteTypeName tnn ON tnn.typeId = tn.id " +
    "JOIN CourseName cn ON cn.course = c " +
    "JOIN cn.language lang " +
    "JOIN UniversityName un ON un.university = d.university " +
    "JOIN un.language ul " +
    "JOIN DepartmentName dn ON dn.department = d " +
    "JOIN dn.language dl " +
    "WHERE ul.code = :language_code " +
    "AND dl.code = :language_code " +
    "AND tnn.language.code = :language_code " +
    "AND lang.code = :language_code " +
    "AND u.id != :userId " +
    "AND (" +
    "    (SELECT COUNT(nl) FROM NoteLike nl WHERE nl.note.id = n.id AND nl.user.id = :userId AND nl.isActive = true) * 3 " +
    "    + (SELECT COUNT(nv) FROM NoteView nv WHERE nv.noteId = n.id AND nv.userId = :userId) >= :threshold" +
    ") " +
    "ORDER BY (" +
    "    (SELECT COUNT(nl) FROM NoteLike nl WHERE nl.note.id = n.id AND nl.user.id = :userId AND nl.isActive = true) + " +
    "    (SELECT COUNT(nv) FROM NoteView nv WHERE nv.note.id = n.id AND nv.userId = :userId)" +
    ") DESC"
)
List<NoteDTO> findTopInteractedNotesByUser(
    @Param("language_code") String language_code,
    @Param("userId") Long userId,
    @Param("threshold") Long threshold
);


   @Query(value = 
      "SELECT TO_CHAR(n.CREATED_AT, 'YYYY-IW') AS upload_week, " +
      "COUNT(n.NOTE_ID) AS total_notes_uploaded " +
      "FROM admin.notes n " +
      "WHERE n.CREATED_AT BETWEEN TO_DATE(:fromDate, 'YYYY-MM-DD') AND TO_DATE(:toDate, 'YYYY-MM-DD') " +
      "AND n.DELETED = 0 " +
      "GROUP BY TO_CHAR(n.CREATED_AT, 'YYYY-IW') " +
      "ORDER BY upload_week",
      nativeQuery = true)
   List<Map<String, Object>> getNoteUploadMetrics(
      @Param("fromDate") String fromDate,
      @Param("toDate") String toDate
   );


   @Query(value = 
        "WITH weekly_notes AS (" +
        "    SELECT TO_CHAR(n.CREATED_AT, 'YYYY-IW') AS upload_week, " +
        "           COUNT(n.NOTE_ID) AS notes_uploaded_this_week " +
        "    FROM admin.notes n " +
        "    WHERE n.DELETED = 0 " +
        "    GROUP BY TO_CHAR(n.CREATED_AT, 'YYYY-IW') " +
        "    ORDER BY upload_week " +
        "), " +
        "cumulative_notes AS (" +
        "    SELECT upload_week, notes_uploaded_this_week, " +
        "           SUM(notes_uploaded_this_week) OVER (ORDER BY upload_week) AS cumulative_total_notes " +
        "    FROM weekly_notes " +
        ") " +
        "SELECT cn.upload_week, " +
        "       cn.cumulative_total_notes, " +
        "       LAG(cn.cumulative_total_notes) OVER (ORDER BY cn.upload_week) AS previous_cumulative_total, " +
        "       CASE WHEN LAG(cn.cumulative_total_notes) OVER (ORDER BY cn.upload_week) > 0 THEN " +
        "            ROUND((cn.cumulative_total_notes - " +
        "                  LAG(cn.cumulative_total_notes) OVER (ORDER BY cn.upload_week)) * 100.0 / " +
        "                  LAG(cn.cumulative_total_notes) OVER (ORDER BY cn.upload_week), 2) " +
        "       ELSE 0 END AS cumulative_percentage_increase " +
        "FROM cumulative_notes cn " +
        "WHERE cn.upload_week BETWEEN TO_CHAR(TO_DATE(:fromDate, 'YYYY-MM-DD'), 'YYYY-IW') AND TO_CHAR(TO_DATE(:toDate, 'YYYY-MM-DD'), 'YYYY-IW') " +
        "ORDER BY cn.upload_week",
        nativeQuery = true)
    List<Map<String, Object>> getContentIncreaseMetrics(
        @Param("fromDate") String fromDate,
        @Param("toDate") String toDate
    );

   List<Note> findByCourse_DepartmentAndIsPublicTrueAndDeletedFalse(Department deptB);

   boolean existsByCourse_DepartmentAndIsPublicTrueAndDeletedFalse(Department deptB);

   @Query("SELECT COUNT(*) FROM Note n WHERE n.isPublic= True AND n.deleted=false AND n.user.id = id")
   Long countNotesByUserId(Long id);

}



