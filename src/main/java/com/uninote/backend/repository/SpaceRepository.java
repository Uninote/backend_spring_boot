package com.uninote.backend.repository;

import com.uninote.backend.dto.SpaceSummaryDTO;
import com.uninote.backend.entity.Space;
import com.uninote.backend.entity.User;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SpaceRepository extends JpaRepository<Space, Long> {

    Optional<Space> findByUuidAndUser_FirebaseUid(String uuid, String userUid);

    Optional<Space> findByUuid(String spaceId);
    List<Space> findAllByUser(User user);
    @Query("SELECT new com.uninote.backend.dto.SpaceSummaryDTO(" +
        "CAST(s.id AS java.lang.Long), s.title, CAST(s.createdAt AS java.sql.Timestamp), s.uuid) " +
        "FROM Space s WHERE s.user = :user AND s.uuid IS NOT NULL AND s.updatedAt IS NOT NULL ORDER BY s.updatedAt DESC")
    List<SpaceSummaryDTO> findAllSummariesByUser(@Param("user") User user);


   
    long countByUserAndCreatedAtBetween(User user, Date startOfDay, Date endOfDay);

}
