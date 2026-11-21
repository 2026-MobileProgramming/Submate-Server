package com.submate.submate.repository;

import com.submate.submate.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository
        extends JpaRepository<EmailVerification, Long> {

    // 해당 이메일, 목적의 가장 최근 인증 기록 조회
    Optional<EmailVerification> findFirstByEmailAndPurposeOrderByCreatedAtDesc(
            String email,
            String purpose
    );

    // 해당 이메일, 목적의 PENDING 인증 기록 조회
    Optional<EmailVerification> findFirstByEmailAndPurposeAndStatusOrderByCreatedAtDesc(
            String email,
            String purpose,
            String status
    );
}