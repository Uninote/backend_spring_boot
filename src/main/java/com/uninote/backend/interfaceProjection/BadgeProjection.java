package com.uninote.backend.interfaceProjection;

public interface BadgeProjection {
    Long getId();
    Long getUserId();
    String getName();
    String getDescription();
    String getImageUrl();
    Integer getRequirement();
    String getTypeName();
    Boolean getUserHasBadge();
}
