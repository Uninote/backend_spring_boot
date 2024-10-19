package com.uninote.backend.repository;

import com.uninote.backend.entity.University;
import com.uninote.backend.interfaceProjection.UniversityDetailsProjection;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UniversityRepository extends JpaRepository<University, Long> {
    Optional<University> findById(Long id);
    List<University> findByLocation(String location);

    @Query("SELECT n.university.id AS id, n.fullName AS fullName, n.name AS name " +
            "FROM UniversityName n JOIN n.language l " +
            "WHERE l.code = :language")
    List<UniversityDetailsProjection> findUniversityDetailsByLanguage(String language);

    @Query(value = "SELECT DISTINCT u.university_id AS id, un.UNIVERSITY_FULL_NAME AS fullName, un.UNIVERSITY_NAME AS name " +
                   "FROM admin.universities u " +
                   "JOIN admin.departments d ON u.university_id = d.university_id " +
                   "JOIN admin.courses c ON d.department_id = c.department_id " +
                   "JOIN admin.questions q ON c.course_id = q.course_id " +
                   "JOIN admin.university_names un ON u.university_id = un.university_id " +
                   "JOIN admin.languages l ON un.language_id = l.language_id " +
                   "WHERE l.language_code = :language " +
                   "AND q.question_id IS NOT NULL",
           nativeQuery = true)
    List<UniversityDetailsProjection> findUniversitiesWithQuestionsInLanguage(@Param("language") String language);
}