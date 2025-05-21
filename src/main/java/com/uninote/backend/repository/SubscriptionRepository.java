package com.uninote.backend.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.uninote.backend.entity.Subscription;
import com.uninote.backend.entity.User;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByStripeSubscriptionId(String subscriptionId);

    boolean existsByUserAndStartDateBeforeAndEndDateAfter(User user, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(s) > 0 FROM Subscription s " +
       "WHERE s.user = :user AND (s.plan = 'FREE' OR s.endDate IS NULL OR s.endDate > CURRENT_TIMESTAMP)")
    boolean existsActiveByUser(@Param("user") User user);

    Optional<Subscription> findActiveByUser(User user);

    boolean existsByUser(User savedUser);

}