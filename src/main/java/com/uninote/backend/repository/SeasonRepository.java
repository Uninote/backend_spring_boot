package com.uninote.backend.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import com.uninote.backend.entity.Season;

import java.util.Optional;

public interface SeasonRepository extends JpaRepository<Season, Long> {
    Optional<Season> findByName(String name);
}
