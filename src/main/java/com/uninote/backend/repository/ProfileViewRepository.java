package com.uninote.backend.repository;

import com.uninote.backend.entity.ProfileView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileViewRepository extends JpaRepository<ProfileView, Long> {
    
}
