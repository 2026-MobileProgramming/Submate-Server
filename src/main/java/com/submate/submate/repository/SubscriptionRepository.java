package com.submate.submate.repository;

import com.submate.submate.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByUserId(Long userId);

    List<Subscription> findByUserIdOrderByIdAsc(Long userId);

    List<Subscription> findByUserIdAndStatus(Long userId, String status);

    // 본인 구독만 조회 (남의 구독이면 비어 있음 → 404)
    Optional<Subscription> findByIdAndUserId(Long id, Long userId);

    // 내 구독 현황 개수 (앱 SubscribeSummary와 같은 기준)
    long countByUserIdAndStatusNot(Long userId, String status);

    long countByUserIdAndPlanTypeAndStatusNot(Long userId, String planType, String status);

    long countByUserIdAndPlanTypeAndStatus(Long userId, String planType, String status);
}
