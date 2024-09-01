package com.uninote.backend.interfaceProjection;

public interface UserProfileProjection {
    Long getId();
    String getFirebaseUid();
    String getName();
    String getDepartmentName();
    String getUniversityName();
    String getDepartmentFullName();
    String getUniversityFullName();
    String getEmail();
    String getUsername();
    String getProfileImageUrl();
    Long getUniscore();
    Long getRoleId();
    String getBio();
    String getRank();
    int getStreak();
    long getTotalNotes();
    long getTotalPublicNotes();
    long getTotalLikes();
}
