package com.uninote.backend.repository;

import com.uninote.backend.entity.Rank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RankRepository extends JpaRepository<Rank, Long> {
    Optional<Rank> findTopByMinScoreLessThanEqualOrderByMinScoreDesc(Long uniscore);
}
