package com.uninote.backend.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.uninote.backend.entity.Subscription;
import com.uninote.backend.entity.User;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByStripeSubscriptionId(String subscriptionId);

    boolean existsByUserAndStartDateBeforeAndEndDateAfter(User user, LocalDateTime start, LocalDateTime end);
}