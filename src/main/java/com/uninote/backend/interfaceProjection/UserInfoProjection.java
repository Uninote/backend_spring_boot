package com.uninote.backend.interfaceProjection;

import org.springframework.beans.factory.annotation.Value;

import com.uninote.backend.entity.SubscriptionPlan;

public interface UserInfoProjection {
    Long getUniversityId();
    Long getDepartmentId();
    Long getUniscore();
    String getUsername();
    String getProfileImageUrl();
    String getRankName();
    Long getUserId();
    String getDepartmentName();
    String getUniversityName();
    String getInstagramUsername();
    Long getSeasonScore();
    @Value("#{(target.certified instanceof T(java.lang.Boolean)) ? target.certified : (target.certified == 1)}")
    Boolean getCertified();
    SubscriptionPlan getSubscriptionPlan();
}
