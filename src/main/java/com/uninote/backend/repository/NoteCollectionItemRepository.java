package com.uninote.backend.repository;

import com.uninote.backend.entity.NoteCollectionItem;
import com.uninote.backend.interfaceProjection.NoteProjection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoteCollectionItemRepository extends JpaRepository<NoteCollectionItem, Long> {
    
    
    //List<NoteCollectionItem> findByCollectionId(Long collectionId);

    @Query("SELECT n.id AS id, n.course.id AS courseId, n.user.id AS userId, n.title AS title, " +
       "n.description AS description, n.pdfUrl AS pdfUrl, n.filename AS filename, " +
       "n.isPublic AS isPublic, n.likes AS likes, u.username AS username, u.profileImageUrl AS profileImageUrl, " +
       "n.createdAt AS createdAt, " +
       "(SELECT cn.name FROM CourseName cn WHERE cn.course.id = n.course.id AND cn.language.code = 'EN') AS courseName, " +
       "(SELECT un.name FROM UniversityName un WHERE un.university.id = d.university.id AND un.language.code = 'EN') AS universityName, " +
       "(SELECT dn.name FROM DepartmentName dn WHERE dn.department.id = d.id AND dn.language.code = 'EN') AS departmentName " +
       "FROM NoteCollectionItem nci " +
       "JOIN Note n ON nci.noteId = n.id " +
       "JOIN Course c ON n.course.id = c.id " +
       "JOIN Department d ON c.department.id = d.id " +
       "JOIN User u ON n.user.id = u.id " +
       "WHERE nci.collectionId = :collectionId AND n.isPublic = True")
    List<NoteProjection> findNoteProjectionsByCollectionId(@Param("collectionId") Long collectionId);


    @Query("SELECT COUNT(n) FROM NoteCollectionItem n WHERE n.collectionId = :collectionId")
    Long countNotesInCollection(@Param("collectionId") Long collectionId);
}
