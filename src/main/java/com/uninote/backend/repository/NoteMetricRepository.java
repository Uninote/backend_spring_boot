package com.uninote.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uninote.backend.dto.NoteMetricDTO;
import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.Note;

@Repository
public interface NoteMetricRepository extends JpaRepository<Department, Long> {

    @Query("SELECT new com.uninote.backend.dto.NoteMetricDTO(d.id, dn.name, un.name, " +
       "(COUNT(DISTINCT n.course.id) * 1.0 / COUNT(DISTINCT c.id)) * 100) " +
       "FROM Department d " +
       "JOIN d.courses c " +
       "JOIN d.university u " +
       "JOIN u.universityNames un " +
       "JOIN d.departmentNames dn " +
       "LEFT JOIN Note n ON n.course.id = c.id AND n.deleted = false " +
       "GROUP BY d.id, dn.name, un.name " +
       "ORDER BY un.name")
List<NoteMetricDTO> findNoteMetrics();




@Query("SELECT c.id AS courseId, cn.name AS courseName, COUNT(n.id) AS noteCount, " +
       "COUNT(DISTINCT n.user.id) AS uniqueCreators " +
       "FROM Course c " +
       "JOIN c.courseNames cn " +  // Assuming courseNames is a valid field in Course
       "LEFT JOIN Note n ON n.course.id = c.id " +  // Join Note entity using course relationship
       "WHERE c.department.id = :departmentId " +
       "GROUP BY c.id, cn.name, c.semester ORDER BY c.semester")
List<Object[]> getNotesCountAndUniqueCreatorsByDepartment(@Param("departmentId") Long departmentId);


}



