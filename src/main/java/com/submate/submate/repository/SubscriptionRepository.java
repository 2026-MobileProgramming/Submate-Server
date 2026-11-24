package com.submate.submate.repository;

import com.submate.submate.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Transactional
    @Modifying
    @Query("""
            update Subscription s set s.status = 'CANCELED', s.updatedAt = :updatedAt
            where (s.status = 'CANCEL_SCHEDULED' and s.cancelDate <= :today)
               or (s.status <> 'CANCELED' and s.planType = 'TRIAL' and s.trialEndDate < :today)
            """)
    int cancelExpired(@Param("today") LocalDate today,
                      @Param("updatedAt") LocalDateTime updatedAt);

    @Query("""
            select s.id from Subscription s
            where s.planType = 'PAID' and s.startDate <= :today
              and (s.status <> 'CANCELED' or s.cancelDate > s.startDate)
            """)
    List<Long> findPaymentCandidateIds(@Param("today") LocalDate today);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Subscription s where s.id = :id")
    Optional<Subscription> findForPaymentRecording(@Param("id") Long id);

    List<Subscription> findByUserId(Long userId);

    List<Subscription> findByUserIdOrderByIdAsc(Long userId);

    List<Subscription> findByUserIdAndStatus(Long userId, String status);

    // 본인 구독만 조회 (남의 구독이면 비어 있음 → 404)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Subscription> findByIdAndUserId(Long id, Long userId);

    // 내 구독 현황 개수 (앱 SubscribeSummary와 같은 기준)
    long countByUserIdAndStatusNot(Long userId, String status);

    long countByUserIdAndPlanTypeAndStatusNot(Long userId, String planType, String status);

    long countByUserIdAndPlanTypeAndStatus(Long userId, String planType, String status);
}
