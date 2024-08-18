package com.uninote.backend.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.University;
import com.uninote.backend.entity.User;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(String id);
    
    @Query("SELECT u FROM User u WHERE u.role.id IN (1, 2) ORDER BY u.uniscore DESC")
    List<User> findTop100ByUniscore();

    @Query("SELECT u FROM User u  WHERE u.role.id IN (1, 2) AND u.department = :department ORDER BY u.uniscore DESC" )
    List<User> findTop100ByUniscoreByDepartment(@Param("department") Department department);

    @Query("SELECT u FROM User u  WHERE u.role.id IN (1, 2) AND u.university = :university ORDER BY u.uniscore DESC" )
    List<User> findTop100ByUniscoreByUniversity(@Param("university") University university);

    Optional<User> findByFirebaseUid(String firebaseUid);

    @Query("SELECT COUNT(n) FROM Note n WHERE n.user.id = :userId")
    long countUserNotes(Long userId);

    @Query("SELECT COUNT(n) FROM Note n WHERE n.user.id = :userId AND n.isPublic = true")
    long countUserPublicNotes(Long userId);

    @Query("SELECT COUNT(nl) FROM NoteLike nl WHERE nl.note.user.id = :userId")
    long countUserLikes(Long userId);
}