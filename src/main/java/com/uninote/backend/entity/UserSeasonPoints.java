package com.uninote.backend.entity;
import javax.persistence.*;

@Entity
public class UserSeasonPoints {

   @EmbeddedId
    private UserSeasonPointsId id;

    
    /*@Column(name = "user_id", nullable = false)
    private Long userId;  

    
    @Column(name = "season_id", nullable = false)
    private Long seasonId;*/

    @Column(nullable = false, columnDefinition = "integer default 0")
    private int points;

    @Column
    private Integer ranking; 

    @Column(nullable = false, columnDefinition = "integer default 0")
    private boolean rewardsClaimed;

    public UserSeasonPoints() {}

    public UserSeasonPoints(UserSeasonPointsId userSeasonPointsId, int points, Integer ranking, boolean rewardsClaimed) {
        //this.userId = user;
        //this.seasonId = season;
        this.id = userSeasonPointsId;
        this.points = points;
        this.ranking = ranking;
        this.rewardsClaimed = rewardsClaimed;
    }

    public UserSeasonPointsId getUserSeasonPointsId() {
        return id;
    }

    public void setId(UserSeasonPointsId id) {
        this.id = id;
    }

   /* public Long getUserId() {
        return userId;
    }

    public void setUserId(Long user) {
        this.userId = user;
    }

    public Long getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(Long season) {
        this.seasonId = season;
    }*/

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public Integer getRanking() {
        return ranking;
    }

    public void setRanking(Integer ranking) {
        this.ranking = ranking;
    }

    public boolean isRewardsClaimed() {
        return rewardsClaimed;
    }

    public void setRewardsClaimed(boolean rewardsClaimed) {
        this.rewardsClaimed = rewardsClaimed;
    }
}
