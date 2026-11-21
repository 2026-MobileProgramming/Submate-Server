package com.submate.submate.repository;

import com.submate.submate.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // 기간 안의 결제 내역 (캘린더·리포트)
    List<Payment> findByUserIdAndPaidDateBetween(Long userId, LocalDate start, LocalDate end);

    // 스케줄러 중복 방지 확인
    boolean existsBySubscriptionIdAndPaidDate(Long subscriptionId, LocalDate paidDate);
}
