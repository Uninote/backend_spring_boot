package com.uninote.backend.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.uninote.backend.entity.Season;
import com.uninote.backend.interfaceProjection.UserInfoProjection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SeasonRepository extends JpaRepository<Season, Long> {
    Optional<Season> findByName(String name);


    @Query("SELECT s FROM Season s WHERE :currentDate BETWEEN s.startDate AND s.endDate")
    Optional<Season> findCurrentSeason(LocalDateTime currentDate);

    @Query("SELECT s FROM Season s WHERE " +
           "(s.startDate < :endDate AND s.endDate > :startDate)")
    Optional<Season> findOverlappingSeason(LocalDateTime startDate, LocalDateTime endDate);

    

}
