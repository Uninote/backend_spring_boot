package com.uninote.backend.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uninote.backend.entity.Subscription;
import com.uninote.backend.entity.SubscriptionDuration;
import com.uninote.backend.entity.SubscriptionPlan;
import com.uninote.backend.entity.User;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByStripeSubscriptionId(String subscriptionId);

    boolean existsByUserAndStartDateBeforeAndEndDateAfter(User user, LocalDateTime start, LocalDateTime end);

    /**
     * Check for overlapping subscriptions based on dates only (ignoring hours)
     * This allows a new subscription to start on the same day that another ends
     */
    @Query("SELECT COUNT(s) > 0 FROM Subscription s " +
           "WHERE s.user = :user AND s.status = 'active' AND " +
           "CAST(s.startDate AS date) < CAST(:endDate AS date) AND " +
           "CAST(s.endDate AS date) > CAST(:startDate AS date)")
    boolean existsOverlappingByUserAndDateRange(@Param("user") User user, 
                                               @Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(s) > 0 FROM Subscription s " +
       "WHERE s.user = :user AND (s.plan = 'FREE' OR s.endDate IS NULL OR s.endDate > CURRENT_TIMESTAMP)")
    boolean existsActiveByUser(@Param("user") User user);

    @Query("SELECT s FROM Subscription s WHERE s.user = :user AND s.status = 'active' AND s.startDate <= :now AND (s.endDate IS NULL OR s.endDate >= :now) AND s.plan <> :freePlan ORDER BY s.startDate DESC")
    Optional<Subscription> findLatestActiveByUser(@Param("user") User user, @Param("now") LocalDateTime now, @Param("freePlan") SubscriptionPlan freePlan);

    boolean existsByUser(User savedUser);

    boolean existsByStripeSubscriptionId(String stripeSubscriptionId);

    boolean existsByUser_IdAndDuration(Long userId, SubscriptionDuration duration);

    Optional<Subscription> findByUser_IdAndDuration(Long userId, SubscriptionDuration duration);

    Optional<Subscription> findByUser_IdAndStatus(Long userId, String status);
}