package com.uninote.backend.repository;

import com.uninote.backend.entity.NoteLike;
import com.uninote.backend.entity.NoteView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface NoteViewRepository extends JpaRepository<NoteView, Long> {
    Optional<NoteView> findByNoteIdAndUserId(Long noteId, Long userId);
    List<NoteView> findByNoteId(Long noteId);
    List<NoteView> findByUserId(Long userId);

    @Query("SELECT v.id, COUNT(v.id) as viewCount FROM NoteView v GROUP BY v.id ORDER BY viewCount DESC")
    List<Object []> findTop10ByOrderByViewCountDesc();

    List<NoteView> findByCreatedAtAfter(LocalDateTime localDateTime);

    @Query("SELECT nv.noteId, AVG(EXTRACT(DAY FROM (nv.viewEndTime - nv.createdAt)) * 86400 + " +
       "EXTRACT(HOUR FROM (nv.viewEndTime - nv.createdAt)) * 3600 + " +
       "EXTRACT(MINUTE FROM (nv.viewEndTime - nv.createdAt)) * 60 + " +
       "EXTRACT(SECOND FROM (nv.viewEndTime - nv.createdAt))) AS avgViewDuration " +
       "FROM NoteView nv " +
       "GROUP BY nv.noteId " +
       "HAVING AVG(EXTRACT(DAY FROM (nv.viewEndTime - nv.createdAt)) * 86400 + " +
       "EXTRACT(HOUR FROM (nv.viewEndTime - nv.createdAt)) * 3600 + " +
       "EXTRACT(MINUTE FROM (nv.viewEndTime - nv.createdAt)) * 60 + " +
       "EXTRACT(SECOND FROM (nv.viewEndTime - nv.createdAt))) > :minAvgViewDuration")
List<Object[]> findNotesWithMinAvgViewDuration(@Param("minAvgViewDuration") double minAvgViewDuration);



    @Query("SELECT nv.note.id " +
       "FROM NoteView nv " +
       "GROUP BY nv.note.id " +
       "HAVING COUNT(nv.id) < :maxViews")
    List<Object[]> findNotesWithFewViews(@Param("maxViews") long maxViews);
    

    @Query("SELECT nv.note.id FROM NoteView nv WHERE nv.userId = :userId")
    Set<Long> findNotesViewedByUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(v) FROM NoteView v WHERE v.userId = :userId AND v.noteId = :noteId")
    int countViewsByUserAndNote(@Param("userId") Long userId, @Param("noteId") Long noteId);
    
}


