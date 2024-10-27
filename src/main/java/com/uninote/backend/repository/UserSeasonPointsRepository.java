package com.uninote.backend.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uninote.backend.entity.Season;
import com.uninote.backend.entity.User;
import com.uninote.backend.entity.UserSeasonPoints;
import com.uninote.backend.interfaceProjection.UserInfoProjection;

import java.util.List;
import java.util.Optional;

public interface UserSeasonPointsRepository extends JpaRepository<UserSeasonPoints, Long> {
    List<UserSeasonPoints> findByIdSeasonId(Long seasonId);
    List<UserSeasonPoints> findByIdUserId(Long userId);
    Optional<UserSeasonPoints> findByIdUserIdAndIdSeasonId(Long userId, Long seasonId);


    @Query(value = "SELECT u.university_id AS universityId, u.department_id AS departmentId, u.uniscore AS uniscore, " +
               "u.username AS username, u.profile_image_url AS profileImageUrl, u.user_id as userId, r.rank_name AS rankName, usp.points AS seasonScore, u.certified AS certified " +
               "FROM admin.users u " +
               "JOIN admin.ranks r ON u.rank_id = r.rank_id " +
               "JOIN admin.user_season_points usp ON usp.user_id = u.user_id "+
               " WHERE u.role_id IN (1, 2)  AND usp.season_id= :seasonId " +
               "ORDER BY usp.points DESC FETCH FIRST 100 ROWS ONLY", 
       nativeQuery = true)
    List<UserInfoProjection> top100UsersPerSeason(@Param("seasonId") Long seasonId);

    @Query(value = "SELECT u.university_id AS universityId, u.department_id AS departmentId, u.uniscore AS uniscore, " +
               "u.username AS username, u.profile_image_url AS profileImageUrl, u.user_id as userId, r.rank_name AS rankName, usp.points AS seasonScore, u.certified AS certified " +
               "FROM admin.users u " +
               "JOIN admin.ranks r ON u.rank_id = r.rank_id " +
               "JOIN admin.departments d ON d.department_id = u.department_id " +
               "JOIN admin.user_season_points usp ON usp.user_id = u.user_id "+
               " WHERE u.role_id IN (1, 2)  AND usp.season_id= :seasonId AND d.university_id = :universityId " +
               "ORDER BY usp.points DESC FETCH FIRST 100 ROWS ONLY", 
       nativeQuery = true)
    List<UserInfoProjection> top100UsersPerSeasonAndUniversity(@Param("seasonId") Long seasonId, @Param("universityId") Long universityId);


    @Query(value = "SELECT u.university_id AS universityId, u.department_id AS departmentId, u.uniscore AS uniscore, " +
               "u.username AS username, u.profile_image_url AS profileImageUrl, u.user_id as userId, r.rank_name AS rankName, usp.points AS seasonScore, u.certified AS certified " +
               "FROM admin.users u " +
               "JOIN admin.ranks r ON u.rank_id = r.rank_id " +
               "JOIN admin.user_season_points usp ON usp.user_id = u.user_id "+
               " WHERE u.role_id IN (1, 2)  AND usp.season_id= :seasonId AND u.department_id = :departmentId " +
               "ORDER BY usp.points DESC FETCH FIRST 100 ROWS ONLY", 
       nativeQuery = true)
    List<UserInfoProjection> top100UsersPerSeasonAndDepartment(@Param("seasonId") Long seasonId, @Param("departmentId") Long departmentId);
}


