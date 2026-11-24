package com.submate.submate.repository;

import com.submate.submate.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // 기간 안의 결제 내역 (캘린더·리포트)
    List<Payment> findByUserIdAndPaidDateBetween(Long userId, LocalDate start, LocalDate end);

    // 기간 안의 결제 내역 + 구독·카테고리 (결제 내역 조회 API, 날짜순)
    @Query("select p from Payment p join fetch p.subscription s join fetch s.category "
            + "where p.user.id = :userId and p.paidDate between :from and :to "
            + "order by p.paidDate asc, p.id asc")
    List<Payment> findAllForPeriod(@Param("userId") Long userId,
                                   @Param("from") LocalDate from,
                                   @Param("to") LocalDate to);

    // 스케줄러 중복 방지 확인
    boolean existsBySubscriptionIdAndPaidDate(Long subscriptionId, LocalDate paidDate);

    @Query("""
            select p.paidDate from Payment p
            where p.subscription.id = :subscriptionId and p.paidDate between :from and :to
            """)
    List<LocalDate> findRecordedDates(@Param("subscriptionId") Long subscriptionId,
                                      @Param("from") LocalDate from, @Param("to") LocalDate to);

    // 리포트용 전체 결제 내역 조회 (구독 및 카테고리 포함)
    @Query("select p from Payment p " +
            "join fetch p.subscription s " +
            "join fetch s.category " +
            "where p.user.id = :userId " +
            "order by p.paidDate asc, p.id asc")
    List<Payment> findAllForReport(@Param("userId") Long userId);
}
