package com.submate.submate.repository;

import com.submate.submate.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByUserId(Long userId);

    List<Subscription> findByUserIdAndStatus(Long userId, String status);

    // 본인 구독만 조회 (남의 구독이면 비어 있음 → 404)
    Optional<Subscription> findByIdAndUserId(Long id, Long userId);
}
