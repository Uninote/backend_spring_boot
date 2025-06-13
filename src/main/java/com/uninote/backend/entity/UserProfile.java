package com.uninote.backend.entity;

import java.util.List;
import javax.persistence.*;

@Entity
@Table(name = "USER_PROFILES", schema = "ADMIN")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PROFILE_ID")
    private Long id;

    @OneToOne
    @JoinColumn(name = "USER_ID")
    private User user;

    @ElementCollection
    @CollectionTable(name = "USER_INTERESTS", joinColumns = @JoinColumn(name = "PROFILE_ID"))
    @Column(name = "INTEREST")
    private List<String> interests;

    @ElementCollection
    @CollectionTable(name = "USER_GOALS", joinColumns = @JoinColumn(name = "PROFILE_ID"))
    @Column(name = "GOAL")
    private List<String> goals;

    @ElementCollection
    @CollectionTable(name = "USER_PERSONALITY_TRAITS", joinColumns = @JoinColumn(name = "PROFILE_ID"))
    @Column(name = "TRAIT")
    private List<String> personality_traits;

    @Column(name = "POSSIBLE_PROFESSION")
    private String possible_profession;

    @Column(name = "POSSIBLE_AGE_RANGE")
    private String possible_age_range;

    @ElementCollection
    @CollectionTable(name = "USER_COMMON_QUESTIONS", joinColumns = @JoinColumn(name = "PROFILE_ID"))
    @Column(name = "QUESTION")
    private List<String> common_questions;

    @ElementCollection
    @CollectionTable(name = "USER_SHORTCOMINGS", joinColumns = @JoinColumn(name = "PROFILE_ID"))
    @Column(name = "SHORTCOMING")
    private List<String> shortcomings;

    @Column(name = "ENGAGEMENT_LEVEL")
    private String engagement_level;

    @Column(name = "GENERAL_DESCRIPTION")
    private String general_description;

    // Getters and Setters
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

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public List<String> getGoals() {
        return goals;
    }

    public void setGoals(List<String> goals) {
        this.goals = goals;
    }

    public List<String> getPersonality_traits() {
        return personality_traits;
    }

    public void setPersonality_traits(List<String> personality_traits) {
        this.personality_traits = personality_traits;
    }

    public String getPossible_profession() {
        return possible_profession;
    }

    public void setPossible_profession(String possible_profession) {
        this.possible_profession = possible_profession;
    }

    public String getPossible_age_range() {
        return possible_age_range;
    }

    public void setPossible_age_range(String possible_age_range) {
        this.possible_age_range = possible_age_range;
    }

    public List<String> getCommon_questions() {
        return common_questions;
    }

    public void setCommon_questions(List<String> common_questions) {
        this.common_questions = common_questions;
    }

    public List<String> getShortcomings() {
        return shortcomings;
    }

    public void setShortcomings(List<String> shortcomings) {
        this.shortcomings = shortcomings;
    }

    public String getEngagement_level() {
        return engagement_level;
    }

    public void setEngagement_level(String engagement_level) {
        this.engagement_level = engagement_level;
    }

    public String getGeneral_description() {
        return general_description;
    }

    public void setGeneral_description(String general_description) {
        this.general_description = general_description;
    }
}
