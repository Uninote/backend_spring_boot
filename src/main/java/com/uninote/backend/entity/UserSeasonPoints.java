package com.uninote.backend.entity;
import javax.persistence.*;

@Entity
public class UserSeasonPoints {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  

    @ManyToOne
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private int points;

    @Column
    private Integer ranking; // Nullable, to store the ranking at the end of the season

    @Column(nullable = false, columnDefinition = "integer default 0")
    private boolean rewardsClaimed;

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Season getSeason() {
        return season;
    }

    public void setSeason(Season season) {
        this.season = season;
    }

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
