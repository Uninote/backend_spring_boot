package com.uninote.backend.repository;


import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uninote.backend.dto.GrowthStatisticsDTO;
import com.uninote.backend.dto.MAUChangeStatisticsDTO;
import com.uninote.backend.dto.MonthlyActiveUsersDTO;
import com.uninote.backend.dto.UserDTO;
import com.uninote.backend.dto.UserGrowthDTO;
import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.University;
import com.uninote.backend.entity.User;
import com.uninote.backend.interfaceProjection.UserInfoProjection;
import com.uninote.backend.interfaceProjection.UserProfileProjection;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(String id);


    @Query(value ="select count(*) from admin.users  where email_verified = 1 and role_id != 21", nativeQuery = true)
    Long countTotalVerifiedUsers();    

    @Query(value ="select count(*) from admin.users where email_verified = 0 and role_id != 21", nativeQuery = true)
    Long countTotalUnverifiedUsers();  

    @Query(value = "SELECT u.university_id AS universityId, u.department_id AS departmentId, u.uniscore AS uniscore, " +
               "u.username AS username, u.profile_image_url AS profileImageUrl, u.user_id as userId, r.rank_name AS rankName,CASE WHEN u.certified = 1 THEN 1 ELSE 0 END AS certified " +
               "FROM admin.users u " +
               "JOIN admin.ranks r ON u.rank_id = r.rank_id " +
               "WHERE u.role_id IN (1, 2) AND u.email_verified = 1 " +
               "ORDER BY u.uniscore DESC " +
               "FETCH FIRST 100 ROWS ONLY", 
       nativeQuery = true)
   List<UserInfoProjection> findTop100ByUniscore();

   @Query(value = "SELECT u.university_id AS universityId, u.department_id AS departmentId, u.uniscore AS uniscore, " +
               "u.username AS username, u.profile_image_url AS profileImageUrl, u.user_id as userId, r.rank_name AS rankName, CASE WHEN u.certified = 1 THEN 1 ELSE 0 END AS certified " +
               "FROM admin.users u " +
               "JOIN admin.ranks r ON u.rank_id = r.rank_id " +
               "WHERE u.role_id IN (1, 2) AND u.department_id = :departmentId AND u.email_verified = 1 " +
               "ORDER BY u.uniscore DESC FETCH FIRST 100 ROWS ONLY", 
       nativeQuery = true)
    List<UserInfoProjection> findTop100ByUniscoreByDepartment(@Param("departmentId") Long departmentId);



    @Query(value = "SELECT u.university_id AS universityId, u.department_id AS departmentId, u.uniscore AS uniscore, " +
               "u.username AS username, u.profile_image_url AS profileImageUrl, u.user_id as userId, r.rank_name AS rankName, CASE WHEN u.certified = 1 THEN 1 ELSE 0 END AS certified " +
               "FROM admin.users u " +
               "JOIN admin.ranks r ON u.rank_id = r.rank_id " +
               " WHERE u.role_id IN (1, 2) AND u.university_id = :universityId AND u.email_verified = 1 " +
               "ORDER BY u.uniscore DESC FETCH FIRST 100 ROWS ONLY", 
       nativeQuery = true)
    List<UserInfoProjection> findTop100ByUniscoreByUniversity(@Param("universityId") Long universityId);



    //Optional<User> findByFirebaseUid(String firebaseUid);

    @Query(value = "SELECT user_id FROM admin.users  WHERE firebase_uid = :firebaseUid", nativeQuery = true)
    Optional<Long> findUserIdByFirebaseUid(@Param("firebaseUid") String firebaseUid);

    @Query("SELECT COUNT(n) FROM Note n WHERE n.user.id = :userId AND n.deleted = false")
    long countUserNotes(Long userId);

    @Query("SELECT COUNT(n) FROM Note n WHERE n.user.id = :userId AND n.isPublic = true AND n.deleted = false")
    long countUserPublicNotes(Long userId);

    @Query("SELECT COUNT(nl) FROM NoteLike nl WHERE nl.note.user.id = :userId")
    long countUserLikes(Long userId);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT u.id AS id, u.firebaseUid AS firebaseUid, u.name AS name, " +
        "dn.name AS departmentName, dn.fullName AS departmentFullName, " +
        "un.name AS universityName, un.fullName AS universityFullName, " +
        "u.email AS email, u.username AS username, u.profileImageUrl AS profileImageUrl, " +
        "u.instagramUsername AS instagramUsername, COALESCE(u.seasonScore, 0) AS seasonScore, " +
        "u.uniscore AS uniscore, u.role.id AS roleId, u.bio AS bio, r.rankName AS rank, u.streak AS streak, " +
        "(SELECT COUNT(n) FROM Note n WHERE n.user.id = u.id and n.deleted = 0) AS totalNotes, " +
        "(SELECT COUNT(n) FROM Note n WHERE n.user.id = u.id AND n.isPublic = true AND n.deleted = 0) AS totalPublicNotes, " +
        "(SELECT COUNT(nl) FROM NoteLike nl WHERE nl.note.user.id = u.id) AS totalLikes, " +
        "u.certified AS certified, " +
        "s.plan AS subscriptionPlan, " +
        "CASE WHEN trial.id IS NOT NULL THEN true ELSE false END AS freeTrialCompleted " +
        "FROM User u " +
        "JOIN DepartmentName dn ON dn.department = u.department " +
        "JOIN UniversityName un ON un.university = u.university " +
        "JOIN Rank r ON r.id = u.rank.id " +
        "LEFT JOIN Subscription s ON s.user = u " +
        "AND s.status = 'active' " +
        "AND s.startDate <= CURRENT_DATE " +
        "AND (s.endDate IS NULL OR s.endDate >= CURRENT_DATE) " +
        "LEFT JOIN Subscription trial ON trial.user = u AND trial.duration = 'THREE_DAYS' " +
        "WHERE dn.language.code = :languageId " +
        "AND un.language.code = :languageId " +
        "AND u.id = :userId")
        UserProfileProjection findUserProfileById(@Param("userId") Long userId, @Param("languageId") String languageId);


    @Query("SELECT u.university.id AS universityId, " +
        "u.department.id AS departmentId, " +
        "u.uniscore AS uniscore, " +
        "u.username AS username, " +
        "u.profileImageUrl AS profileImageUrl, " +
        "u.instagramUsername AS instagramUsername, " +
        "COALESCE(u.seasonScore, 0) AS seasonScore, " +
        "dn.name AS departmentName, " +
        "un.name AS universityName, " +
        "u.certified AS certified, " +
        "s.plan AS subscriptionPlan, " +
        "CASE WHEN trial.id IS NOT NULL THEN true ELSE false END AS freeTrialCompleted " +
        "FROM User u " +
        "JOIN DepartmentName dn ON dn.department = u.department " +
        "JOIN UniversityName un ON un.university = u.university " +
        "LEFT JOIN Subscription s ON s.user = u " +
        "AND s.status = 'active' " +
        "AND s.startDate <= CURRENT_DATE " +
        "AND (s.endDate IS NULL OR s.endDate >= CURRENT_DATE) " +
        "LEFT JOIN Subscription trial ON trial.user = u AND trial.duration = 'THREE_DAYS' " +
        "WHERE dn.language.code = :language " +
        "AND un.language.code = :language " +
        "AND u.id = :userId")
        UserInfoProjection findUserInfoById(@Param("userId") Long userId, @Param("language") String language);


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


        List<User> findAllByAndEmailVerifiedTrueAndUsernameIsNotNull();

        List<User> findAllByUsernameIsNotNull();



        
        @Query("SELECT u.id FROM User u where u.emailVerified = true")
        List<Long> findUserIds();

        @Query(value = "SELECT COUNT(*) FROM ADMIN.USERS WHERE ROLE_ID = 21", nativeQuery = true)
        Long getTotalDeletedUsers();

        @Query(value = "SELECT " +
               "  (SELECT COUNT(*) * 100.0 / total FROM admin.users u WHERE u.created_at >= :sevenDaysAgo) AS lastWeek, " +
               "  (SELECT COUNT(*) * 100.0 / total FROM admin.users u WHERE u.created_at >= :oneMonthAgo) AS lastMonth, " +
               "  (SELECT COUNT(*) * 100.0 / total FROM admin.users u WHERE u.created_at >= :threeMonthsAgo) AS lastQuarter, " +
               "  (SELECT COUNT(*) * 100.0 / total FROM admin.users u WHERE u.created_at >= :oneYearAgo) AS lastYear, " +
               "  total " +
               "FROM (SELECT COUNT(*) AS total FROM admin.users) totalData",
       nativeQuery = true)
List<Object[]> getGrowthStatisticsNative(@Param("sevenDaysAgo") LocalDate sevenDaysAgo,
                                         @Param("oneMonthAgo") LocalDate oneMonthAgo,
                                         @Param("threeMonthsAgo") LocalDate threeMonthsAgo,
                                         @Param("oneYearAgo") LocalDate oneYearAgo);








        @Query("SELECT new com.uninote.backend.dto.UserGrowthDTO(TO_CHAR(u.createdAt, 'YYYY-MM') AS month, COUNT(u) AS totalUsers) " +
                "FROM User u " +
                "GROUP BY TO_CHAR(u.createdAt, 'YYYY-MM') " +
                "ORDER BY month")
        List<UserGrowthDTO> getUserGrowthOverTime();

        @Query("SELECT new com.uninote.backend.dto.MonthlyActiveUsersDTO(FUNCTION('TO_CHAR', ul.loginTimestamp, 'YYYY-MM'), COUNT(DISTINCT ul.user.id)) " +
        "FROM UserLogin ul " +
        "WHERE ul.loginTimestamp IS NOT NULL " +
        "GROUP BY FUNCTION('TO_CHAR', ul.loginTimestamp, 'YYYY-MM') " +
        "ORDER BY FUNCTION('TO_CHAR', ul.loginTimestamp, 'YYYY-MM')")
        List<MonthlyActiveUsersDTO> getMonthlyActiveUsers();


        @Query("SELECT new com.uninote.backend.dto.MonthlyActiveUsersDTO(" +
       "    FUNCTION('TO_CHAR', ul.loginTimestamp, 'YYYY-MM') AS month, " +
       "    COUNT(DISTINCT ul.user.id) AS activeUsers, " +
       "    ROUND((COUNT(DISTINCT ul.user.id) * 100.0) / (" +
       "        SELECT COUNT(u.id) FROM User u WHERE FUNCTION('TO_CHAR', u.createdAt, 'YYYY-MM') <= FUNCTION('TO_CHAR', ul.loginTimestamp, 'YYYY-MM')" +
       "    ), 2) AS activeUserPercentage " +
       ") " +
       "FROM UserLogin ul " +
       "WHERE ul.loginTimestamp IS NOT NULL " +
       "GROUP BY FUNCTION('TO_CHAR', ul.loginTimestamp, 'YYYY-MM') " +
       "ORDER BY FUNCTION('TO_CHAR', ul.loginTimestamp, 'YYYY-MM')")
    List<MonthlyActiveUsersDTO> getMonthlyActiveUserPercentage();


        


        

    @Query(value = 
        "WITH registration_week AS (" +
        "    SELECT u.USER_ID, TO_CHAR(u.CREATED_AT, 'YYYY-IW') AS registration_week " +
        "    FROM admin.users u " +
        "    WHERE u.CREATED_AT BETWEEN TO_DATE(:fromDate, 'YYYY-MM-DD') AND TO_DATE(:toDate, 'YYYY-MM-DD') " +
        "), " +
        "weekly_logins AS (" +
        "    SELECT ul.USER_ID, TO_CHAR(ul.LOGIN_TIMESTAMP, 'YYYY-IW') AS login_week " +
        "    FROM admin.user_logins ul " +
        "    INNER JOIN registration_week rw ON ul.USER_ID = rw.USER_ID " +
        "), " +
        "initial_registration AS (" +
        "    SELECT registration_week, COUNT(DISTINCT USER_ID) AS initial_users " +
        "    FROM registration_week " +
        "    GROUP BY registration_week " +
        ") " +
        "SELECT rw.registration_week, wl.login_week, COUNT(DISTINCT wl.USER_ID) AS active_users, " +
        "ROUND((COUNT(DISTINCT wl.USER_ID) * 100.0) / ir.initial_users, 2) AS retention_rate " +
        "FROM registration_week rw " +
        "LEFT JOIN weekly_logins wl ON rw.USER_ID = wl.USER_ID " +
        "LEFT JOIN initial_registration ir ON rw.registration_week = ir.registration_week " +
        "GROUP BY rw.registration_week, wl.login_week, ir.initial_users " +
        "ORDER BY rw.registration_week, wl.login_week",
        nativeQuery = true)
    List<Map<String, Object>> getRetentionRate(
        @Param("fromDate") String fromDate,
        @Param("toDate") String toDate
    );


    Optional<User> findByFirebaseUid(String firebaseUid);


    List<User> findByEmailVerifiedFalse();


    Optional<User> findByEmail(String email);

    
}