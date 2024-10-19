package com.uninote.backend.entity;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;


@Embeddable
public class UserSeasonPointsId implements Serializable{

    @Column(name = "season_id")
    private Long seasonId;

    @Column(name = "user_id")
    private Long userId;
    public UserSeasonPointsId() {}

    public Long getUserId() {
        return userId;
    }

    public Long getSeasonId() {
        return seasonId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setSeasonId(Long seasonId) {
        this.seasonId = seasonId;
    }

     @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSeasonPointsId that = (UserSeasonPointsId) o;
        return Objects.equals(seasonId, that.seasonId) &&
               Objects.equals(userId, that.userId);
    }

    
    @Override
    public int hashCode() {
        return Objects.hash(seasonId, userId);
    }
    
}
