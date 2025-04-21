package com.uninote.backend.repository;

import com.uninote.backend.entity.YouTubeResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface YouTubeResourceRepository extends JpaRepository<YouTubeResource, Long> {
    // YouTube-specific queries can go here
}
