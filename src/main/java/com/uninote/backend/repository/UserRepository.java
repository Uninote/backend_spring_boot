package com.uninote.backend.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uninote.backend.dto.UserDTO;
import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.University;
import com.uninote.backend.entity.User;
import com.uninote.backend.interfaceProjection.UserInfoProjection;
import com.uninote.backend.interfaceProjection.UserProfileProjection;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(String id);
    
    @Query(value = "SELECT u.university_id AS universityId, u.department_id AS departmentId, u.uniscore AS uniscore, " +
               "u.username AS username, u.profile_image_url AS profileImageUrl, u.user_id as userId, r.rank_name AS rankName " +
               "FROM admin.users u " +
               "JOIN admin.ranks r ON u.rank_id = r.rank_id " +
               "WHERE u.role_id IN (1, 2) " +
               "ORDER BY u.uniscore DESC " +
               "FETCH FIRST 100 ROWS ONLY", 
       nativeQuery = true)
   List<UserInfoProjection> findTop100ByUniscore();

   @Query(value = "SELECT u.university_id AS universityId, u.department_id AS departmentId, u.uniscore AS uniscore, " +
               "u.username AS username, u.profile_image_url AS profileImageUrl, u.user_id as userId, r.rank_name AS rankName " +
               "FROM admin.users u " +
               "JOIN admin.ranks r ON u.rank_id = r.rank_id " +
               "WHERE u.role_id IN (1, 2) AND u.department_id = :departmentId " +
               "ORDER BY u.uniscore DESC FETCH FIRST 100 ROWS ONLY", 
       nativeQuery = true)
    List<UserInfoProjection> findTop100ByUniscoreByDepartment(@Param("departmentId") Long departmentId);



    @Query(value = "SELECT u.university_id AS universityId, u.department_id AS departmentId, u.uniscore AS uniscore, " +
               "u.username AS username, u.profile_image_url AS profileImageUrl, u.user_id as userId, r.rank_name AS rankName " +
               "FROM admin.users u " +
               "JOIN admin.ranks r ON u.rank_id = r.rank_id " +
               " WHERE u.role_id IN (1, 2) AND u.university_id = :universityId " +
               "ORDER BY u.uniscore DESC FETCH FIRST 100 ROWS ONLY", 
       nativeQuery = true)
    List<UserInfoProjection> findTop100ByUniscoreByUniversity(@Param("universityId") Long universityId);



    //Optional<User> findByFirebaseUid(String firebaseUid);

    @Query(value = "SELECT user_id FROM admin.users  WHERE firebase_uid = :firebaseUid", nativeQuery = true)
    Optional<Long> findUserIdByFirebaseUid(@Param("firebaseUid") String firebaseUid);

    @Query("SELECT COUNT(n) FROM Note n WHERE n.user.id = :userId")
    long countUserNotes(Long userId);

    @Query("SELECT COUNT(n) FROM Note n WHERE n.user.id = :userId AND n.isPublic = true")
    long countUserPublicNotes(Long userId);

    @Query("SELECT COUNT(nl) FROM NoteLike nl WHERE nl.note.user.id = :userId")
    long countUserLikes(Long userId);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT u.id AS id, u.firebaseUid AS firebaseUid, u.name AS name, " +
       "dn.name AS departmentName, dn.fullName AS departmentFullName, " +
       "un.name AS universityName, un.fullName AS universityFullName, " +
       "u.email AS email, u.username AS username, u.profileImageUrl AS profileImageUrl, u.instagramUsername AS instagramUsername, " +
       "u.uniscore AS uniscore, u.role.id AS roleId, u.bio AS bio, r.rankName AS rank, u.streak AS streak, " +
       "(SELECT COUNT(n) FROM Note n WHERE n.user.id = u.id) AS totalNotes, " +
       "(SELECT COUNT(n) FROM Note n WHERE n.user.id = u.id AND n.isPublic = true) AS totalPublicNotes, " +
       "(SELECT COUNT(nl) FROM NoteLike nl WHERE nl.note.user.id = u.id) AS totalLikes " +
       "FROM User u " +
       "JOIN DepartmentName dn ON dn.department = u.department " +
       "JOIN UniversityName un ON un.university = u.university " +
       "JOIN Rank r ON r.id = u.rank.id " +
       "WHERE dn.language.id = :languageId " +
       "AND un.language.id = :languageId " +
       "AND u.id = :userId")
    UserProfileProjection findUserProfileById(@Param("userId") Long userId, @Param("languageId") Long languageId);

    @Query("SELECT u.university.id AS universityId, u.department.id AS departmentId, " +
      "u.uniscore AS uniscore, u.username AS username, u.profileImageUrl AS profileImageUrl, u.instagramUsername AS instagramUsername, " +
      "dn.name AS departmentName, " +
      "un.name AS universityName " +
      "FROM User u " +
      "JOIN DepartmentName dn ON dn.department = u.department AND dn.language.id = :languageId " +
      "JOIN UniversityName un ON un.university = u.university AND un.language.id = :languageId " +
      "WHERE u.id = :userId")
   UserInfoProjection findUserInfoById(@Param("userId") Long userId, @Param("languageId") Long languageId);

   @Query(value = "SELECT rank FROM (" +
               "  SELECT u.user_id, RANK() OVER (PARTITION BY u.department_id ORDER BY u.uniscore DESC) AS rank " +
               "  FROM admin.users u " +
               "  WHERE u.role_id IN (1, 2)" +
               ") ranked_users WHERE ranked_users.user_id = :userId", 
       nativeQuery = true)
   Integer findUserRankInDepartment(@Param("userId") Long userId);


   @Query(value = "SELECT rank FROM (" +
               "  SELECT u.user_id, RANK() OVER (PARTITION BY u.university_id ORDER BY u.uniscore DESC) AS rank " +
               "  FROM admin.users u " +
               "  WHERE u.role_id IN (1, 2)" +
               ") ranked_users WHERE ranked_users.user_id = :userId", 
       nativeQuery = true)
   Integer findUserRankInUniversity(@Param("userId") Long userId);



   @Query(value = "SELECT rank FROM (" +
               "  SELECT u.user_id, RANK() OVER (ORDER BY u.uniscore DESC) AS rank " +
               "  FROM admin.users u " +
               "  WHERE u.role_id IN (1, 2)" +
               ") ranked_users WHERE ranked_users.user_id = :userId", 
       nativeQuery = true)
Integer findUserGlobalRank(@Param("userId") Long userId);


        @Query("SELECT u.id FROM User u WHERE u.username = :username")
      Optional<Long> findUserIdByUsername(@Param("username") String username);

        @Query(value = "SELECT email FROM admin.users u WHERE username = :username", nativeQuery = true)
        Optional<String> findUserEmailByUsername(@Param("username") String username);




}