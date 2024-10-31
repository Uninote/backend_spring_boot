package com.uninote.backend.dto;



public class RankDTO {

    private Long minScore;
    private String rankName;


    public RankDTO(Long score, String name) {
        this.minScore = score;
        this.rankName = name;
    }


    public Long getMinScore() {
        return minScore;
    }    

    public String getName() {
        return rankName;
    }

}
