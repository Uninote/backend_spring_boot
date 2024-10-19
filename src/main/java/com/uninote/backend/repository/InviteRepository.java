package com.uninote.backend.repository;

import com.uninote.backend.entity.Invite;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InviteRepository extends JpaRepository<Invite, Long> {
    long countByUserId(Long userId);
    List<Invite> findByUserId(Long userId);
    Optional<Invite> findByUuid(String uuid);    
    long countByUserIdAndInviteeIsNotNull(Long userId);
}
