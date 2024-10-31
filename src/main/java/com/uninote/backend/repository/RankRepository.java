package com.uninote.backend.repository;

import com.uninote.backend.dto.RankDTO;
import com.uninote.backend.entity.Rank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RankRepository extends JpaRepository<Rank, Long> {
    Optional<Rank> findTopByMinScoreLessThanEqualOrderByMinScoreDesc(Long uniscore);


    @Query("SELECT new com.uninote.backend.dto.RankDTO(r.minScore, r.rankName) FROM Rank r ORDER BY r.minScore DESC")
    public List<RankDTO> findAllDto();
}
