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
import org.springframework.data.jpa.repository.Modifying;
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
       "n.likes , u.username, u.profileImageUrl, n.createdAt, tn.typeName, n.professor, n.academicYear) " +
       "FROM Note n " +
       "JOIN n.course c " +
       "LEFT JOIN n.noteType tn " +
       "JOIN c.department d " +
       "JOIN n.user u " +
       "WHERE n.user.id = :userId AND  n.deleted = false")
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
               "n.likes, u.username, u.profileImageUrl, n.createdAt, tn.typeName, n.professor, n.academicYear ) " +
               "FROM Note n " +
               "JOIN n.course c " +
               "JOIN c.department d " +
               "JOIN n.user u " +
               "JOIN CourseName cn ON cn.course = c " +
               "JOIN cn.language l " +
               "LEFT JOIN n.noteType tn "+  
               "JOIN UniversityName un ON un.university = d.university " +
               "JOIN un.language ul " +  
               "JOIN DepartmentName dn ON dn.department = d " +
               "JOIN dn.language dl " +  
               "WHERE l.code = 'EN' AND ul.code = 'EN' AND dl.code = 'EN' " +
               "AND n.isPublic = true AND n.deleted = false")
Page<NoteDTO> findPublicNotes(Pageable pageable);

    @Query("SELECT n FROM Note n WHERE n.isPublic = true AND n.user = :user AND n.course.department = :department")
    List<Note> findPublicNotesByUserAndDepartment(@Param("user") User user, @Param("department") Department department);

    @Query(value = "SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, " +
               "n.filename, n.isPublic, cn.name, un.name, dn.name, " +
               "n.likes, u.username, u.profileImageUrl, n.createdAt, tn.typeName, n.professor, n.academicYear ) " +
               "FROM Note n " +
               "JOIN n.course c " +
               "JOIN c.department d " +
               "JOIN n.user u " +
               "LEFT JOIN n.noteType tn " + 
               "JOIN CourseName cn ON cn.course = c " +
               "JOIN cn.language l " +  
               "JOIN UniversityName un ON un.university = d.university " +
               "JOIN un.language ul " +  
               "JOIN DepartmentName dn ON dn.department = d " +
               "JOIN dn.language dl " +  
               "WHERE l.code = 'EN' AND ul.code = 'EN' AND dl.code = 'EN' " +
               "AND n.isPublic = true AND d = :department AND n.deleted = false")
Page<NoteDTO> findPublicNotesByDepartment(@Param("department") Department department,  Pageable pageable);



@Query(value = "SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, " +
               "n.filename, n.isPublic, cn.name, un.name, dn.name, " +
               "n.likes, u.username, u.profileImageUrl, n.createdAt, tn.typeName, n.professor, n.academicYear ) " +
               "FROM Note n " +
               "JOIN n.course c " +
               "JOIN c.department d " +
               "JOIN n.user u " +
               "LEFT JOIN n.noteType tn " +
               "JOIN CourseName cn ON cn.course = c " +
               "JOIN cn.language l " +  
               "JOIN UniversityName un ON un.university = d.university " +
               "JOIN un.language ul " +  
               "JOIN DepartmentName dn ON dn.department = d " +
               "JOIN dn.language dl " +  
               "WHERE l.code = 'EN' AND ul.code = 'EN' AND dl.code = 'EN' " +
               "AND n.isPublic = true AND n.course.department.university = :university AND  n.deleted = false")
   Page<NoteDTO> findPublicNotesByUniversity(@Param("university") University university, Pageable pageable);

    @Query(value = "SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, " +
               "n.filename, n.isPublic, cn.name, un.name, dn.name, " +
               "n.likes, u.username, u.profileImageUrl, n.createdAt, tn.typeName, n.professor, n.academicYear ) " +
               "FROM Note n " +
               "JOIN n.course c " +
               "JOIN c.department d " +
               "JOIN n.user u " +
               "LEFT JOIN n.noteType tn " +
               "JOIN CourseName cn ON cn.course = c " +
               "JOIN cn.language l " +  
               "JOIN UniversityName un ON un.university = d.university " +
               "JOIN un.language ul " +  
               "JOIN DepartmentName dn ON dn.department = d " +
               "JOIN dn.language dl " +  
               "WHERE l.code = 'EN' AND ul.code = 'EN' AND dl.code = 'EN' " +
               "AND n.isPublic = true AND n.course = :course AND n.deleted = false")
    Page<NoteDTO> findPublicNotesByCourse(@Param("course") Course course, Pageable pageable);

    @Query(value = "SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, " +
               "n.filename, n.isPublic, cn.name, un.name, dn.name, " +
               "n.likes, u.username, u.profileImageUrl, n.createdAt, tn.typeName, n.professor, n.academicYear ) " +
               "FROM Note n " +
               "JOIN n.course c " +
               "JOIN c.department d " +
               "JOIN n.user u " +
               "LEFT JOIN n.noteType tn " +
               "JOIN CourseName cn ON cn.course = c " +
               "JOIN cn.language l " +  
               "JOIN UniversityName un ON un.university = d.university " +
               "JOIN un.language ul " +  
               "JOIN DepartmentName dn ON dn.department = d " +
               "JOIN dn.language dl " +  
               "WHERE l.code = 'EN' AND ul.code = 'EN' AND dl.code = 'EN' " +
               "AND n.isPublic = true AND n.course.department.id = :departmentId AND n.course.semester = :semester AND n.deleted = false")
    Page<NoteDTO> findPublicNotesByDepartmentAndSemester(@Param("departmentId") Long departmentId, @Param("semester") int semester, Pageable pageable);

    @Query("SELECT n FROM Note n WHERE n.isPublic = true AND n.user = :user AND n.course.department.university = :university")
    List<Note> findPublicNotesByUserAndUniversity(@Param("user") User user, @Param("university") University university);

    @Query("SELECT n FROM Note n WHERE n.isPublic = true AND n.user = :user AND n.course = :course")
    List<Note> findPublicNotesByUserAndCourse(@Param("user") User user, @Param("course") Course course);

    @Query("SELECT n FROM Note n WHERE n.isPublic = true AND n.user = :user AND n.course.department.id = :departmentId AND n.course.semester = :semester")
    List<Note> findPublicNotesByUserAndDepartmentAndSemester(@Param("user") User user, @Param("departmentId") Long departmentId, @Param("semester") int semester);

    @Query("SELECT COUNT(n) FROM Note n WHERE n.user.id = :userId AND n.deleted = false")
    long countByUserId(@Param("userId") Long userId);


    @Query("SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, n.filename, n.isPublic, " +
       "(SELECT cn.name FROM CourseName cn WHERE cn.course = c AND cn.language.code = 'EN'), " +
       "(SELECT un.name FROM UniversityName un WHERE un.university = d.university AND un.language.code = 'EN'), " +
       "(SELECT dn.name FROM DepartmentName dn WHERE dn.department = d AND dn.language.code = 'EN'), " +
       "n.likes, u.username, u.profileImageUrl, n.createdAt, tn.typeName, n.professor, n.academicYear) " +
       "FROM NoteSave ns " +
       "JOIN ns.note n " +
       "LEFT JOIN n.noteType tn " +
       "JOIN n.course c " +
       "JOIN c.department d " +
       "JOIN n.user u " +
       "WHERE ns.user.id = :userId AND ns.isActive = TRUE AND n.isPublic = TRUE AND n.deleted = false")
   List<NoteDTO> findPublicSavedNotesByUserId(@Param("userId") Long userId);

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
               "JOIN admin.courses c ON n.course_id = c.course_id " +
               "JOIN admin.departments d ON c.department_id = d.department_id " +
               "JOIN admin.users u ON n.user_id = u.user_id " +
               "WHERE n.is_public = 1 AND u.user_id = :userId AND n.deleted = 0 " +
               "ORDER BY n.like_count DESC, n.created_at DESC " +
               "FETCH FIRST :limit ROWS ONLY", nativeQuery = true)
    List<NoteProjection> findTopPublicNotesByUser(@Param("userId") Long userId, @Param("limit") int limit);

    
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

    @Query(value = "SELECT n.note_id, c.course_id, u.user_id, n.title, n.description, n.pdf_url, " +
               "n.filename, n.is_public, cn.course_name, un.university_name, dn.department_name, " +
               "n.like_count as like_count, u.username, u.profile_image_url, n.created_at, n.professor, n.academic_year, tn.type_name " +
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
               "WHERE n.is_public = 1 AND n.deleted = 0 " +
               "AND l.language_code = 'EN' AND ul.language_code = 'EN' AND dl.language_code = 'EN' " +
               "AND (" +
               "UTL_MATCH.EDIT_DISTANCE(LOWER(REPLACE(REPLACE(NVL(n.title, ''), ' ', ''), '-', '')), LOWER(REPLACE(REPLACE(:keyword, ' ', ''), '-', ''))) <= LEAST(LENGTH(:keyword), LENGTH(NVL(n.title, ''))) * :threshold OR " +
               "UTL_MATCH.EDIT_DISTANCE(LOWER(REPLACE(REPLACE(NVL(n.description, ''), ' ', ''), '-', '')), LOWER(REPLACE(REPLACE(:keyword, ' ', ''), '-', ''))) <= LEAST(LENGTH(:keyword), LENGTH(NVL(n.description, ''))) * :threshold OR " +
               "UTL_MATCH.EDIT_DISTANCE(LOWER(REPLACE(REPLACE(NVL(cn.course_name, ''), ' ', ''), '-', '')), LOWER(REPLACE(REPLACE(:keyword, ' ', ''), '-', ''))) <= LEAST(LENGTH(:keyword), LENGTH(NVL(cn.course_name, ''))) * :threshold OR " +
               "UTL_MATCH.EDIT_DISTANCE(LOWER(REPLACE(REPLACE(NVL(un.university_name, ''), ' ', ''), '-', '')), LOWER(REPLACE(REPLACE(:keyword, ' ', ''), '-', ''))) <= LEAST(LENGTH(:keyword), LENGTH(NVL(un.university_name, ''))) * :threshold OR " +
               "UTL_MATCH.EDIT_DISTANCE(LOWER(REPLACE(REPLACE(NVL(dn.department_name, ''), ' ', ''), '-', '')), LOWER(REPLACE(REPLACE(:keyword, ' ', ''), '-', ''))) <= LEAST(LENGTH(:keyword), LENGTH(NVL(dn.department_name, ''))) * :threshold" +
               ") " +
               "ORDER BY n.like_count DESC, n.created_at DESC", 
       nativeQuery = true)
Page<Object[]> searchNotesWithEditDistance(@Param("keyword") String keyword, 
                                                
                                               @Param("threshold") double threshold, 
                                               Pageable pageable);



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
               "n.likes, u.username, u.profileImageUrl, n.createdAt) " +
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
               "WHERE l.code = 'EN' AND ul.code = 'EN' AND dl.code = 'EN' " +
               "AND n.isPublic = true AND d = :department AND n   .deleted = false ORDER BY n.createdAt desc")
   Page<NoteDTO> findRecentPublicNotesByDepartment(@Param("department") Department department,  Pageable pageable);

   @Query(value = "SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, " +
               "n.filename, n.isPublic, cn.name, un.name, dn.name, " +
               "n.likes, u.username, u.profileImageUrl, n.createdAt, tn.typeName, n.professor, n.academicYear) " +
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
               "WHERE l.code = 'EN' AND ul.code = 'EN' AND dl.code = 'EN' AND n.noteType.typeId = :typeId " +
               "AND n.isPublic = true AND d = :department AND n.deleted = false ")
   Page<NoteDTO> findPublicNotesByDepartmentByType(@Param("department") Department department,@Param("typeId") Long typeId,  Pageable pageable);


   
   @Query(value = "SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, " +
               "n.filename, n.isPublic, cn.name, un.name, dn.name, " +
               "n.likes, u.username, u.profileImageUrl, n.createdAt, tn.typeName, n.professor, n.academicYear) " +
               "FROM Note n " +
               "JOIN n.course c " +
               "JOIN c.department d " +
               "JOIN n.user u " +
               "LEFT JOIN n.noteType tn " +
               "JOIN CourseName cn ON cn.course = c " +
               "JOIN cn.language l " +  
               "JOIN UniversityName un ON un.university = d.university " +
               "JOIN un.language ul " +  
               "JOIN DepartmentName dn ON dn.department = d " +
               "JOIN dn.language dl " +  
               "WHERE l.code = 'EN' AND ul.code = 'EN' AND dl.code = 'EN' AND n.noteType.typeId = :typeId " +
               "AND n.isPublic = true AND n.course.department.university = :university AND  n.deleted = false")
   Page<NoteDTO> findPublicNotesByUniversityByType(@Param("university") University university, @Param("typeId") Long typeId,Pageable pageable);

    @Query(value = "SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, " +
               "n.filename, n.isPublic, cn.name, un.name, dn.name, " +
               "n.likes, u.username, u.profileImageUrl, n.createdAt, tn.typeName, n.professor, n.academicYear) " +
               "FROM Note n " +
               "JOIN n.course c " +
               "JOIN c.department d " +
               "JOIN n.user u " +
               "LEFT JOIN n.noteType tn " +
               "JOIN CourseName cn ON cn.course = c " +
               "JOIN cn.language l " +  
               "JOIN UniversityName un ON un.university = d.university " +
               "JOIN un.language ul " +  
               "JOIN DepartmentName dn ON dn.department = d " +
               "JOIN dn.language dl " +  
               "WHERE l.code = 'EN' AND ul.code = 'EN' AND dl.code = 'EN' AND n.noteType.typeId = :typeId " +
               "AND n.isPublic = true AND n.course = :course AND n.deleted = false")
    Page<NoteDTO> findPublicNotesByCourseByType(@Param("course") Course course, @Param("typeId") Long typeId,Pageable pageable);

    @Query(value = "SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, " +
    "n.filename, n.isPublic, cn.name, un.name, dn.name, " +
    "n.likes, u.username, u.profileImageUrl, n.createdAt, tn.typeName, n.professor, n.academicYear) " +
    "FROM Note n " +
    "JOIN n.course c " +
    "JOIN c.department d " +
    "JOIN n.user u " +
    "LEFT JOIN n.noteType tn " +
    "JOIN CourseName cn ON cn.course = c " +
    "JOIN cn.language l " +  
    "JOIN UniversityName un ON un.university = d.university " +
    "JOIN un.language ul " +  
    "JOIN DepartmentName dn ON dn.department = d " +
    "JOIN dn.language dl " +  
    "WHERE l.code = 'EN' AND ul.code = 'EN' AND dl.code = 'EN' AND n.noteType.typeId = :typeId " +
    "AND n.isPublic = true AND n.course.department.id = :departmentId AND n.course.semester = :semester AND n.deleted = false")
    Page<NoteDTO> findPublicNotesByDepartmentAndSemesterByType(@Param("departmentId") Long departmentId, @Param("semester") int semester, @Param("typeId") Long typeId,Pageable pageable);


    @Query(value = "SELECT new com.uninote.backend.dto.NoteDTO(n.id, c.id, u.id, n.title, n.description, n.pdfUrl, " +
            "n.filename, n.isPublic, cn.name, un.name, dn.name, " +
            "n.likes, u.username, u.profileImageUrl, n.createdAt, tn.typeName, n.professor, n.academicYear) " +
            "FROM Note n " +
            "JOIN n.course c " +
            "JOIN c.department d " +
            "JOIN n.user u " +
            "LEFT JOIN n.noteType tn " +
            "JOIN CourseName cn ON cn.course = c " +
            "JOIN cn.language l " +  
            "JOIN UniversityName un ON un.university = d.university " +
            "JOIN un.language ul " +  
            "JOIN DepartmentName dn ON dn.department = d " +
            "JOIN dn.language dl " +  
            "WHERE l.code = 'EN' AND ul.code = 'EN' AND dl.code = 'EN' AND n.noteType.typeId = :typeId " +
            "AND n.isPublic = true AND n.deleted = false")
   Page<NoteDTO> findPublicNotesByType(Pageable pageable, @Param("typeId") Long typeId);

}

