package com.uninote.backend.repository;

import com.uninote.backend.dto.CommentDTO;
import com.uninote.backend.entity.Comment;
import com.uninote.backend.entity.Note;
import com.uninote.backend.interfaceProjection.CommentProjection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByNote(Note note);

    @Query(value = "SELECT c.comment_id AS commentId, c.note_id AS noteId, c.user_id AS userId, DBMS_LOB.SUBSTR(c.content, 4000, 1) AS content, c.created_at AS createdAt, " +
               "(SELECT COUNT(*) FROM comment_likes cl WHERE cl.comment_id = c.comment_id) AS totalLikes, " +
               "u.profile_image_url AS profileImageUrl, u.username AS username " +
               "FROM comments c " +
               "JOIN users u ON c.user_id = u.user_id " +
               "WHERE c.note_id = :noteId",
       nativeQuery = true)
    List<CommentProjection> findCommentProjectionsByNoteId(@Param("noteId") Long noteId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT comment_id FROM comments WHERE user_id = :userId", nativeQuery =  true)
    List<Long> findCommentIdsByUserId(@Param("userId") Long userId);


    

}
