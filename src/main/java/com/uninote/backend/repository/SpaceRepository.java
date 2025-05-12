package com.uninote.backend.repository;

import com.uninote.backend.entity.Space;
import com.uninote.backend.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpaceRepository extends JpaRepository<Space, Long> {

    Optional<Space> findByUuidAndUser_FirebaseUid(String uuid, String userUid);

    Optional<Space> findByUuid(String spaceId);
    List<Space> findAllByUser(User user);

    long countByUserAndCreatedAtBetween(User user, LocalDateTime startOfDay, LocalDateTime endOfDay);

}
