package com.uninote.backend.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "username"),
    @UniqueConstraint(columnNames = "email"),
    @UniqueConstraint(columnNames = "firebase_uid")
})
public class User {

    private static final String DEFAULT_PROFILE_URL = "https://firebasestorage.googleapis.com/v0/b/uninote-app.appspot.com/o/images%2Fdefault-images%2Fdefault-woman-pfp.png?alt=media&token=d148c633-ef3f-4161-bf97-413c74415c3d";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "seq_user_id", allocationSize = 1)
    @Column(name = "user_id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "firebase_uid", nullable = false, unique = true)
    private String firebaseUid;

    @Lob
    @Column(name = "bio", columnDefinition = "CLOB")
    private String bio;


    @Column(name = "streak", nullable = false)
    private int streak = 0;

    @Column(name = "uniscore", nullable = false)
    private Long uniscore = (long) 0;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "last_login", nullable = false)
    private LocalDateTime lastLogin = LocalDateTime.now();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

   
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne
    @JoinColumn(name = "rank_id", nullable = false)
    private Rank rank;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "banner_url")
    private String bannerUrl;


    @ManyToMany
    @JoinTable(
        name = "user_approvals",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "approved_user_id")
    )
    private Set<User> approvedUsers = new HashSet<>();

    @ManyToMany(mappedBy = "approvedUsers")
    private Set<User> approvedByUsers = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (streak == 0) {
            streak = 0;
        }
        if (uniscore == 0) {
            uniscore =(long) 0;
        }
        if (rank == null) {
            rank = new Rank();
            rank.setId(1L); 
        }
        if (profileImageUrl==null) {
            profileImageUrl = DEFAULT_PROFILE_URL;
        }

        updatedAt = LocalDateTime.now();
        lastLogin = LocalDateTime.now();
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public User() {}

   
    public User(Long id, String firebaseUid, int streak, long uniscore, LocalDateTime updatedAt, LocalDateTime lastLogin, String name, Department department, University university, String email, Rank rank, String username, String profileImageUrl) {
        this.id = id;
        this.firebaseUid = firebaseUid;
        this.streak = streak;
        this.uniscore = uniscore;
        this.updatedAt = updatedAt;
        this.lastLogin = lastLogin;
        this.name = name;
        this.department = department;
        this.university = university;
        this.email = email;
        this.rank = rank;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        
    }

   public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public long getUniscore() {
        return uniscore;
    }

    public void setUniscore(long uniscore) {
        this.uniscore = uniscore;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getBio() {
        return bio;
    }

    public LocalDateTime getCreatedAt() {
        return updatedAt;
    }

    public void setCreatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public University getUniversity() {
        return university;
    }

    public void setUniversity(University university) {
        this.university = university;
    }

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public Set<User> getApprovedUsers() {
        return approvedUsers;
    }

    public void setApprovedUsers(Set<User> approvedUsers) {
        this.approvedUsers = approvedUsers;
    }

    public Set<User> getApprovedByUsers() {
        return approvedByUsers;
    }

    public void setApprovedByUsers(Set<User> approvedByUsers) {
        this.approvedByUsers = approvedByUsers;
    }
}
