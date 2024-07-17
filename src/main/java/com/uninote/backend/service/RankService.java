package com.uninote.backend.service;

import com.uninote.backend.entity.Rank;
import com.uninote.backend.repository.RankRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RankService {

    @Autowired
    private RankRepository rankRepository;

    public Rank determineRank(int uniscore) {
        return rankRepository.findTopByMinScoreLessThanEqualOrderByMinScoreDesc(uniscore)
                .orElseThrow(() -> new IllegalArgumentException("No appropriate rank found for the given uniscore."));
    }
}
