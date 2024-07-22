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
    
    @Query("SELECT u FROM User u ORDER BY u.uniscore DESC")
    List<User> findTop100ByUniscore();

    @Query("SELECT u FROM User u  WHERE u.department = :department ORDER BY u.uniscore DESC" )
    List<User> findTop100ByUniscoreByDepartment(@Param("department") Department department);

    @Query("SELECT u FROM User u  WHERE u.university = :university ORDER BY u.uniscore DESC" )
    List<User> findTop100ByUniscoreByUniversity(@Param("university") University university);

    Optional<User> findByFirebaseUid(String firebaseUid);
}