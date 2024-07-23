package com.uninote.backend.repository;

import com.uninote.backend.entity.Invite;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InviteRepository extends JpaRepository<Invite, Long> {
    long countByUserId(Long userId);
    List<Invite> findByUserId(Long userId);

}
