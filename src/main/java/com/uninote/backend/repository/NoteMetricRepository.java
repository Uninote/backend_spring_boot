package com.uninote.backend.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uninote.backend.dto.NoteMetricDTO;
import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.Note;

@Repository
public interface NoteMetricRepository extends JpaRepository<Department, Long> {

    @Query("SELECT new com.uninote.backend.dto.NoteMetricDTO(d.id, dn.fullName, un.fullName, " +
       "  (COUNT(DISTINCT n.course.id) * 1.0 / COUNT(DISTINCT c.id)) * 100) " +
       "FROM Department d " +
       "JOIN d.courses c " +
       "JOIN d.university u " +
       "JOIN u.universityNames un " +
       "JOIN d.departmentNames dn " +
       "LEFT JOIN Note n ON n.course.id = c.id AND n.deleted = false " +
       "WHERE un.language.code = 'GR' AND dn.language.code = 'GR' " +
       "GROUP BY d.id, dn.fullName, un.fullName " +
       "ORDER BY un.fullName")
List<NoteMetricDTO> findNoteMetrics();





@Query("SELECT c.id AS courseId, cn.name AS courseName, COUNT(n.id) AS noteCount, " +
       "COUNT(DISTINCT n.user.id) AS uniqueCreators " +
       "FROM Course c " +
       "JOIN c.courseNames cn " +  // Assuming courseNames is a valid field in Course
       "LEFT JOIN Note n ON n.course.id = c.id " +  // Join Note entity using course relationship
       "WHERE c.department.id = :departmentId " +
       "GROUP BY c.id, cn.name, c.semester ORDER BY c.semester")
List<Object[]> getNotesCountAndUniqueCreatorsByDepartment(@Param("departmentId") Long departmentId);

@Query(value =  "SELECT DATE_TRUNC('day', created_at) AS day, COUNT(*) AS count FROM note_views WHERE created_at >= CURRENT_DATE - INTERVAL '30 days' GROUP BY DATE_TRUNC('day', created_at) ORDER BY day", nativeQuery = true)
    List<Object[]> countLast30daysNoteViews();





    @Query(value = "SELECT DATE_TRUNC('week', day) AS week_start, " +
               "AVG(daily_views) AS average_views " +
               "FROM ( " +
               "    SELECT DATE_TRUNC('day', created_at) AS day, COUNT(note_view_id) AS daily_views " +
               "    FROM note_views " +
               "    WHERE created_at BETWEEN :fromDate::timestamp AND :toDate::timestamp " +
               "    GROUP BY DATE_TRUNC('day', created_at) " +
               ") temp " +
               "GROUP BY DATE_TRUNC('week', day) " +
               "ORDER BY week_start", nativeQuery = true)
    List<Map<String, Object>> findWeeklyAverageNoteViews(
        @Param("fromDate") String fromDate,
        @Param("toDate") String toDate
    );
    



}



