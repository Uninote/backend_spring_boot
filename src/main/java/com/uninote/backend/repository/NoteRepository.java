package com.uninote.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uninote.backend.dto.CourseNameDTO;
import com.uninote.backend.dto.NoteDTO;
import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.Note;
import com.uninote.backend.entity.University;
import com.uninote.backend.entity.User;
import com.uninote.backend.interfaceProjection.NoteProjection;

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
       "n.likes , u.username, u.profileImageUrl, n.createdAt, tnn.typeName, n.professor, n.academicYear, u.certified, ucg.grade,n.status ,n.uuid) " +
       "FROM Note n " +
       "JOIN n.user u " +
       "JOIN n.course c " +
       "LEFT JOIN UserCourseGrade ucg ON ucg.userId = u.id and ucg.courseId = c.id "+
       "LEFT JOIN n.noteType tn " +
       "LEFT JOIN NoteTypeName tnn ON tnn.typeId = tn.id " +
       "JOIN c.department d " +
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
               "n.likes, u.username, u.profileImageUrl, n.createdAt, tnn.typeName, n.professor, n.academicYear, u.certified, ucg.grade, n.status, n.uuid ) " +
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
               "n.likes, u.username, u.profileImageUrl, n.createdAt, tnn.typeName, n.professor, n.academicYear, u.certified, ucg.grade, n.status, n.uuid ) " +
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
               "tnn.typeName, n.professor, n.academicYear, u.certified, ucg.grade, n.status, n.uuid ) " +
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
               "AND n.isPublic = true AND n.course.department.university = :university " +
               "AND n.deleted = false")
Page<NoteDTO> findPublicNotesByUniversity(@Param("university") University university, 
                                          @Param("language_code") String languageCode, 
                                          Pageable pageable);


    @Query(value = "SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, " +
               "n.filename, n.isPublic, cn.name, un.name, dn.name, " +
               "n.likes, u.username, u.profileImageUrl, n.createdAt, tnn.typeName, n.professor, n.academicYear, u.certified, ucg.grade, n.status, n.uuid ) " +
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
               "n.likes, u.username, u.profileImageUrl, n.createdAt, tnn.typeName, n.professor, n.academicYear, u.certified, ucg.grade, n.status, n.uuid ) " +
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
               "substring(n.description, 1, 4000) AS description, n.pdf_url AS pdfUrl, n.filename AS filename, " +
               "(SELECT cn.course_name FROM course_names cn " +
               "JOIN languages l ON cn.language_id = l.language_id " +
               "WHERE cn.course_id = c.course_id AND l.language_code = :language_code) AS courseName, " +
               "(SELECT un.university_name FROM university_names un " +
               "JOIN languages l ON un.language_id = l.language_id " +
               "WHERE un.university_id = d.university_id AND l.language_code = :language_code) AS universityName, " +
               "(SELECT dn.department_name FROM department_names dn " +
               "JOIN languages l ON dn.language_id = l.language_id " +
               "WHERE dn.department_id = d.department_id AND l.language_code = :language_code) AS departmentName, " +
               "n.like_count AS likes, u.username AS username, u.profile_image_url AS profileImageUrl, n.created_at AS createdAt " +
               "FROM notes n " +
               "JOIN courses c ON n.course_id = c.course_id " +
               "JOIN departments d ON c.department_id = d.department_id " +
               "JOIN users u ON n.user_id = u.user_id " +
               "WHERE n.is_public = true AND u.user_id = :userId AND n.deleted = false " +
               "ORDER BY n.like_count DESC, n.created_at DESC " +
               "LIMIT :limit", nativeQuery = true)
    List<NoteProjection> findTopPublicNotesByUser(@Param("userId") Long userId, @Param("language_code") String language_code,@Param("limit") int limit);

    
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
               "FROM notes n " +
               "JOIN courses c ON n.course_id = c.course_id " +
               "JOIN departments d ON c.department_id = d.department_id " +
               "JOIN users u ON n.user_id = u.user_id " +
               "LEFT JOIN note_types tn ON n.type_id = tn.type_id " +
               "JOIN course_names cn ON c.course_id = cn.course_id " +
               "JOIN languages l ON cn.language_id = l.language_id " +
               "JOIN universities univ ON d.university_id = univ.university_id " +
               "JOIN university_names un ON univ.university_id = un.university_id " +
               "JOIN languages ul ON un.language_id = ul.language_id " +
               "JOIN department_names dn ON d.department_id = dn.department_id " +
               "JOIN languages dl ON dn.language_id = dl.language_id " +
               "WHERE  n.deleted = false " +
               "AND l.language_code = 'EN' AND ul.language_code = 'EN' AND dl.language_code = 'EN' " +
               "AND (" +
               "LEVENSHTEIN(LOWER(REPLACE(REPLACE(n.title, ' ', ''), '-', '')), LOWER(REPLACE(REPLACE(:keyword, ' ', ''), '-', ''))) <= :threshold OR " +
               "LEVENSHTEIN(LOWER(REPLACE(REPLACE(n.description, ' ', ''), '-', '')), LOWER(REPLACE(REPLACE(:keyword, ' ', ''), '-', ''))) <= :threshold OR " +
               "LEVENSHTEIN(LOWER(REPLACE(REPLACE(cn.course_name, ' ', ''), '-', '')), LOWER(REPLACE(REPLACE(:keyword, ' ', ''), '-', ''))) <= :threshold OR " +
               "LEVENSHTEIN(LOWER(REPLACE(REPLACE(un.university_name, ' ', ''), '-', '')), LOWER(REPLACE(REPLACE(:keyword, ' ', ''), '-', ''))) <= :threshold OR " +
               "LEVENSHTEIN(LOWER(REPLACE(REPLACE(dn.department_name, ' ', ''), '-', '')), LOWER(REPLACE(REPLACE(:keyword, ' ', ''), '-', ''))) <= :threshold" +
               ") " +
               "ORDER BY n.like_count DESC, n.created_at DESC", 
       nativeQuery = true)
Page<Object[]> searchUserNotesSimple(@Param("keyword") String keyword, 

                                               @Param("threshold") int threshold, 
                                               Pageable pageable);
    @Query(value = "SELECT n.note_id, c.course_id, u.user_id, n.title, n.description, n.pdf_url, " +
               "n.filename, n.is_public, cn.course_name, un.university_name, dn.department_name, " +
               "n.like_count, u.username, u.profile_image_url, n.created_at, n.professor, n.academic_year, tn.type_name, u.certified " +
               "FROM notes n " +
               "JOIN courses c ON n.course_id = c.course_id " +
               "JOIN departments d ON c.department_id = d.department_id " +
               "JOIN users u ON n.user_id = u.user_id " +
               "LEFT JOIN note_types tn ON n.type_id = tn.type_id " +
               "JOIN course_names cn ON c.course_id = cn.course_id " +
               "JOIN languages l ON cn.language_id = l.language_id " +
               "JOIN universities univ ON d.university_id = univ.university_id " +
               "JOIN university_names un ON univ.university_id = un.university_id " +
               "JOIN languages ul ON un.language_id = ul.language_id " +
               "JOIN department_names dn ON d.department_id = dn.department_id " +
               "JOIN languages dl ON dn.language_id = dl.language_id " +
               "WHERE n.user_id = :userId AND n.deleted = false " +
               "AND l.language_code = 'EN' AND ul.language_code = 'EN' AND dl.language_code = 'EN' " +
               "AND (" +
               "LEVENSHTEIN(LOWER(REPLACE(REPLACE(n.title, ' ', ''), '-', '')), LOWER(REPLACE(REPLACE(:keyword, ' ', ''), '-', ''))) <= :threshold OR " +
               "LEVENSHTEIN(LOWER(REPLACE(REPLACE(n.description, ' ', ''), '-', '')), LOWER(REPLACE(REPLACE(:keyword, ' ', ''), '-', ''))) <= :threshold OR " +
               "LEVENSHTEIN(LOWER(REPLACE(REPLACE(cn.course_name, ' ', ''), '-', '')), LOWER(REPLACE(REPLACE(:keyword, ' ', ''), '-', ''))) <= :threshold OR " +
               "LEVENSHTEIN(LOWER(REPLACE(REPLACE(un.university_name, ' ', ''), '-', '')), LOWER(REPLACE(REPLACE(:keyword, ' ', ''), '-', ''))) <= :threshold OR " +
               "LEVENSHTEIN(LOWER(REPLACE(REPLACE(dn.department_name, ' ', ''), '-', '')), LOWER(REPLACE(REPLACE(:keyword, ' ', ''), '-', ''))) <= :threshold" +
               ") " +
               "ORDER BY n.like_count DESC, n.created_at DESC", 
       nativeQuery = true)
Page<Object[]> searchUserNotesWithEditDistance(@Param("keyword") String keyword, 
                                               @Param("userId") Long userId, 
                                               @Param("threshold") int threshold, 
                                               Pageable pageable);


                                               @Query(value = "WITH RECURSIVE note_words AS (" +
                                               "SELECT n.note_id, " +
                                               "       regexp_split_to_table(LOWER(REPLACE(COALESCE(n.title, ''), '-', '')), '\\s+') AS word " +
                                               "FROM notes n " +
                                               "), course_words AS (" +
                                               "SELECT c.course_id, " +
                                               "       regexp_split_to_table(LOWER(REPLACE(COALESCE(cn.course_name, ''), '-', '')), '\\s+') AS word " +
                                               "FROM courses c " +
                                               "JOIN course_names cn ON c.course_id = cn.course_id " +
                                               "), university_words AS (" +
                                               "SELECT univ.university_id, " +
                                               "       regexp_split_to_table(LOWER(REPLACE(COALESCE(un.university_name, ''), '-', '')), '\\s+') AS word " +
                                               "FROM universities univ " +
                                               "JOIN university_names un ON univ.university_id = un.university_id " +
                                               "), department_words AS (" +
                                               "SELECT d.department_id, " +
                                               "       regexp_split_to_table(LOWER(REPLACE(COALESCE(dn.department_name, ''), '-', '')), '\\s+') AS word " +
                                               "FROM departments d " +
                                               "JOIN department_names dn ON d.department_id = dn.department_id " +
                                               ") " +
                                               "SELECT * FROM (" +
                                               "SELECT n.note_id AS id, " +
                                               "       c.course_id AS courseId, " +
                                               "       u.user_id AS userId, " +
                                               "       n.title AS title, " +
                                               "       substring(n.description, 1, 4000) AS description, " +
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
                                               "FROM notes n " +
                                               "JOIN courses c ON n.course_id = c.course_id " +
                                               "JOIN departments d ON c.department_id = d.department_id " +
                                               "JOIN users u ON n.user_id = u.user_id " +
                                               "LEFT JOIN note_types tn ON n.type_id = tn.type_id " +
                                               "JOIN course_names cn ON c.course_id = cn.course_id " +
                                               "JOIN languages l1 ON cn.language_id = l1.language_id AND l1.language_code = 'EN' " +
                                               "JOIN universities univ ON d.university_id = univ.university_id " +
                                               "JOIN university_names un ON univ.university_id = un.university_id " +
                                               "JOIN languages l2 ON un.language_id = l2.language_id AND l2.language_code = 'EN' " +
                                               "JOIN department_names dn ON d.department_id = dn.department_id " +
                                               "JOIN languages l3 ON dn.language_id = l3.language_id AND l3.language_code = 'EN' " +
                                               "LEFT JOIN note_words nw ON nw.note_id = n.note_id AND LEVENSHTEIN(nw.word, LOWER(REPLACE(:keyword, ' ', ''))) <= CEIL(:threshold * length(nw.word)) " +
                                               "LEFT JOIN course_words cw ON cw.course_id = c.course_id AND LEVENSHTEIN(cw.word, LOWER(REPLACE(:keyword, ' ', ''))) <= CEIL(:threshold * length(cw.word)) " +
                                               "LEFT JOIN university_words uw ON uw.university_id = univ.university_id AND LEVENSHTEIN(uw.word, LOWER(REPLACE(:keyword, ' ', ''))) <= CEIL(:threshold * length(uw.word)) " +
                                               "LEFT JOIN department_words depw ON depw.department_id = d.department_id AND LEVENSHTEIN(depw.word, LOWER(REPLACE(:keyword, ' ', ''))) <= CEIL(:threshold * length(depw.word)) " +
                                               "JOIN user_course_grades ucg ON ucg.user_id = u.user_id and ucg.course_id = c.course_id "+
                                               "WHERE n.is_public = true AND n.deleted = false " +
                                               "AND (nw.word IS NOT NULL OR cw.word IS NOT NULL OR uw.word IS NOT NULL OR depw.word IS NOT NULL)) " +
                                               "WHERE row_number BETWEEN :start_row AND :end_row " +
                                               "ORDER BY relevance_score DESC", 
                                       nativeQuery = true)
                                List<Object[]> searchNotes(
                                        @Param("keyword") String keyword, 
                                        @Param("threshold") float threshold, 
                                        @Param("start_row") int startRow, 
                                        @Param("end_row") int endRow);



                                        @Query(value = "WITH RECURSIVE note_words AS (" +
                   "    SELECT n.note_id, " +
                   "           regexp_split_to_table(LOWER(REPLACE(COALESCE(n.title, ''), '-', '')), '\\s+') AS word " +
                   "    FROM notes n " +
                   "), " +
                   "course_words AS (" +
                   "    SELECT c.course_id, " +
                   "           regexp_split_to_table(LOWER(REPLACE(COALESCE(cn.course_name, ''), '-', '')), '\\s+') AS word " +
                   "    FROM courses c " +
                   "    JOIN course_names cn ON c.course_id = cn.course_id " +
                   "), " +
                   "university_words AS (" +
                   "    SELECT univ.university_id, " +
                   "           regexp_split_to_table(LOWER(REPLACE(COALESCE(un.university_name, ''), '-', '')), '\\s+') AS word " +
                   "    FROM universities univ " +
                   "    JOIN university_names un ON univ.university_id = un.university_id " +
                   "), " +
                   "department_words AS (" +
                   "    SELECT d.department_id, " +
                   "           regexp_split_to_table(LOWER(REPLACE(COALESCE(dn.department_name, ''), '-', '')), '\\s+') AS word " +
                   "    FROM departments d " +
                   "    JOIN department_names dn ON d.department_id = dn.department_id " +
                   "), " +
                   "total_elements AS (" +
                   "    SELECT COUNT(*) AS total_count " +
                   "    FROM notes n " +
                   "    JOIN courses c ON n.course_id = c.course_id " +
                   "    JOIN departments d ON c.department_id = d.department_id " +
                   "    JOIN users u ON n.user_id = u.user_id " +
                   "    JOIN course_names cn ON c.course_id = cn.course_id " +
                   "    JOIN languages l1 ON cn.language_id = l1.language_id AND l1.language_code = :language_code " +
                   "    JOIN universities univ ON d.university_id = univ.university_id " +
                   "    JOIN university_names un ON univ.university_id = un.university_id " +
                   "    JOIN languages l2 ON un.language_id = l2.language_id AND l2.language_code = :language_code " +
                   "    JOIN department_names dn ON d.department_id = dn.department_id " +
                   "    JOIN languages l3 ON dn.language_id = l3.language_id AND l3.language_code = :language_code " +
                   "    LEFT JOIN note_words nw ON nw.note_id = n.note_id AND LEVENSHTEIN(nw.word, LOWER(REPLACE(:keyword, ' ', ''))) <= CEIL(:threshold * length(nw.word)) " +
                   "    LEFT JOIN course_words cw ON cw.course_id = c.course_id AND LEVENSHTEIN(cw.word, LOWER(REPLACE(:keyword, ' ', ''))) <= CEIL(:threshold * length(cw.word)) " +
                   "    LEFT JOIN university_words uw ON uw.university_id = univ.university_id AND LEVENSHTEIN(uw.word, LOWER(REPLACE(:keyword, ' ', ''))) <= CEIL(:threshold * length(uw.word)) " +
                   "    LEFT JOIN department_words depw ON depw.department_id = d.department_id AND LEVENSHTEIN(depw.word, LOWER(REPLACE(:keyword, ' ', ''))) <= CEIL(:threshold * length(depw.word)) " +
                   "    WHERE n.is_public = true AND n.deleted = false " +
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
                   "           substring(n.description, 1, 4000) AS description, " +
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
                   "    FROM notes n " +
                   "    JOIN courses c ON n.course_id = c.course_id " +
                   "    JOIN departments d ON c.department_id = d.department_id " +
                   "    JOIN users u ON n.user_id = u.user_id " +
                   "    LEFT JOIN note_types tn ON n.type_id = tn.type_id " +
                   "    JOIN course_names cn ON c.course_id = cn.course_id " +
                   "    JOIN languages l1 ON cn.language_id = l1.language_id AND l1.language_code = :language_code " +
                   "    JOIN universities univ ON d.university_id = univ.university_id " +
                   "    JOIN university_names un ON univ.university_id = un.university_id " +
                   "    JOIN languages l2 ON un.language_id = l2.language_id AND l2.language_code = :language_code " +
                   "    JOIN department_names dn ON d.department_id = dn.department_id " +
                   "    JOIN languages l3 ON dn.language_id = l3.language_id AND l3.language_code = :language_code " +
                   "    LEFT JOIN note_words nw ON nw.note_id = n.note_id AND LEVENSHTEIN(nw.word, LOWER(REPLACE(:keyword, ' ', ''))) <= CEIL(:threshold * length(nw.word)) " +
                   "    LEFT JOIN course_words cw ON cw.course_id = c.course_id AND LEVENSHTEIN(cw.word, LOWER(REPLACE(:keyword, ' ', ''))) <= CEIL(:threshold * length(cw.word)) " +
                   "    LEFT JOIN university_words uw ON uw.university_id = univ.university_id AND LEVENSHTEIN(uw.word, LOWER(REPLACE(:keyword, ' ', ''))) <= CEIL(:threshold * length(uw.word)) " +
                   "    LEFT JOIN department_words depw ON depw.department_id = d.department_id AND LEVENSHTEIN(depw.word, LOWER(REPLACE(:keyword, ' ', ''))) <= CEIL(:threshold * length(depw.word)) " +
                   "    LEFT JOIN user_course_grades ucg ON ucg.user_id = n.user_id and ucg.course_id = n.course_id "+
                   "    WHERE n.is_public = true AND n.deleted = false " +
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
               "substring(n.description, 1, 4000) AS description, n.pdf_url AS pdfUrl, n.filename AS filename, " +
               "(SELECT cn.course_name FROM course_names cn " +
               "JOIN languages l ON cn.language_id = l.language_id " +
               "WHERE cn.course_id = c.course_id AND l.language_code = 'EN') AS courseName, " +
               "(SELECT un.university_name FROM university_names un " +
               "JOIN languages l ON un.language_id = l.language_id " +
               "WHERE un.university_id = d.university_id AND l.language_code = 'EN') AS universityName, " +
               "(SELECT dn.department_name FROM department_names dn " +
               "JOIN languages l ON dn.language_id = l.language_id " +
               "WHERE dn.department_id = d.department_id AND l.language_code = 'EN') AS departmentName, " +
               "n.like_count AS likes, u.username AS username, u.profileImageUrl AS profileImageUrl, n.created_at AS createdAt " +
               "FROM notes n " +
               "JOIN note_collection_items ci ON n.note_id = ci.note_id " +  
               "JOIN courses c ON n.course_id = c.course_id " +
               "JOIN departments d ON c.department_id = d.department_id " +
               "JOIN users u ON n.user_id = u.user_id " +
               "WHERE ci.collection_id = :collectionId " +  
               "ORDER BY n.created_at ASC " +  
               "LIMIT 1", nativeQuery = true)
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
               "n.likes, u.username, u.profileImageUrl, n.createdAt, tnn.typeName, n.professor, n.academicYear, u.certified, ucg.grade, n.status, n.uuid ) " +
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
               "n.likes, u.username, u.profileImageUrl, n.createdAt, tnn.typeName, n.professor, n.academicYear, u.certified, ucg.grade, n.status, n.uuid ) " +
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
               "n.likes, u.username, u.profileImageUrl, n.createdAt, tnn.typeName, n.professor, n.academicYear, u.certified, ucg.grade, n.status, n.uuid) " +
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
    "n.likes, u.username, u.profileImageUrl, n.createdAt, tnn.typeName, n.professor, n.academicYear, u.certified, ucg.grade, n.status, n.uuid  ) " +
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
    "n.likes, u.username, u.profileImageUrl, n.createdAt, tnn.typeName, n.professor, n.academicYear, u.certified, ucg.grade, n.status, n.uuid  ) " +
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



   @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM notes WHERE course_id = :courseId", nativeQuery = true)
   boolean existsByCourseId(@Param("courseId") Long courseId);



   @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END " +
   "FROM notes n " +
   "JOIN courses c ON n.course_id = c.course_id " +
   "JOIN departments d ON c.department_id = d.department_id " +
   "WHERE d.department_id = :departmentId", 
nativeQuery = true)
boolean existsByDepartmentId(@Param("departmentId") Long departmentId);


@Query("SELECT DISTINCT new com.uninote.backend.dto.CourseNameDTO(c.id, cn.name, 'EN') " +
       "FROM Note n " +
       "JOIN n.course c " +
       "JOIN c.courseNames cn " +
       "JOIN cn.language l " +
       "WHERE n.user.id = :userId AND "+
       "n.deleted = false AND l.code = :languageCode")
List<CourseNameDTO> findCoursesWithNotesByUserId(@Param("userId") Long userId, @Param("languageCode") String languageCode);




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
               "WHERE  ul.code = :language_code AND dl.code = :language_code AND tnn.language.code = :language_code " +
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
      "SELECT TO_CHAR(n.created_at, 'YYYY-IW') AS upload_week, " +
      "COUNT(n.note_id) AS total_notes_uploaded " +
      "FROM notes n " +
      "WHERE n.created_at BETWEEN :fromDate::date AND :toDate::date " +
      "AND n.deleted = false " +
      "GROUP BY TO_CHAR(n.created_at, 'YYYY-IW') " +
      "ORDER BY upload_week",
      nativeQuery = true)
   List<Map<String, Object>> getNoteUploadMetrics(
      @Param("fromDate") String fromDate,
      @Param("toDate") String toDate
   );


   @Query(value = 
        "WITH weekly_notes AS (" +
        "    SELECT TO_CHAR(n.created_at, 'YYYY-IW') AS upload_week, " +
        "           COUNT(n.note_id) AS notes_uploaded_this_week " +
        "    FROM notes n " +
        "    WHERE n.deleted = false " +
        "    GROUP BY TO_CHAR(n.created_at, 'YYYY-IW') " +
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
        "WHERE cn.upload_week BETWEEN TO_CHAR(:fromDate::date, 'YYYY-IW') AND TO_CHAR(:toDate::date, 'YYYY-IW') " +
        "ORDER BY cn.upload_week",
        nativeQuery = true)
    List<Map<String, Object>> getContentIncreaseMetrics(
        @Param("fromDate") String fromDate,
        @Param("toDate") String toDate
    );

   List<Note> findByCourse_DepartmentAndIsPublicTrueAndDeletedFalse(Department deptB);

   boolean existsByCourse_DepartmentAndIsPublicTrueAndDeletedFalse(Department deptB);

   @Query("SELECT COUNT(*) FROM Note n WHERE n.isPublic= True AND n.deleted=false AND n.user.id = :id")
   Long countNotesByUserId(Long id);

   @Query("SELECT n.id FROM Note n WHERE (n.content IS NULL OR TRIM(n.content) = '') AND n.pdfUrl IS NOT NULL AND n.filename IS NOT NULL")
   Page<Long> findNoteIdsWithoutContent(Pageable pageable);

   

}


