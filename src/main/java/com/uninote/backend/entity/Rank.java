package com.uninote.backend.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "ranks", uniqueConstraints = {
    @UniqueConstraint(columnNames = "rank_name")
})
public class Rank {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rank_seq")
    @SequenceGenerator(name = "rank_seq", sequenceName = "rank_seq", allocationSize = 1)
    @Column(name = "rank_id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "rank_name", nullable = false, unique = true)
    private String rankName;

    @Column(name = "min_score", nullable = false)
    private Long minScore;

    @OneToMany(mappedBy = "rank", cascade = CascadeType.ALL, orphanRemoval = false)
    private Set<User> users = new HashSet<>();

    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRankName() {
        return rankName;
    }

    public void setRankName(String rankName) {
        this.rankName = rankName;
    }

    public Long getMinScore() {
        return minScore;
    }

    public void setMinScore(Long minScore) {
        this.minScore = minScore;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }
}
