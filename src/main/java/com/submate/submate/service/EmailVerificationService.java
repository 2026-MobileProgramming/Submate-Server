package com.submate.submate.service;

import com.submate.submate.dto.EmailVerificationSendResponse;
import com.submate.submate.entity.EmailVerification;
import com.submate.submate.repository.EmailVerificationRepository;
import com.submate.submate.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    public EmailVerificationService(
            EmailVerificationRepository emailVerificationRepository,
            UserRepository userRepository,
            EmailService emailService
    ) {
        this.emailVerificationRepository = emailVerificationRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }
    @Transactional
    public EmailVerificationSendResponse sendVerificationCode(
            String email,
            String purpose
    ) {

        // 1. 이메일 입력값 검사
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("INVALID_EMAIL");
        }

        email = email.trim().toLowerCase();

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("INVALID_EMAIL");
        }

        // 2. 인증 목적 검사
        if (purpose == null ||
                (!purpose.equals("SIGNUP") && !purpose.equals("RESET_PW"))) {
            throw new IllegalArgumentException("INVALID_PURPOSE");
        }

        // 3. 회원가입 이메일 중복 검사
        if (purpose.equals("SIGNUP")
                && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("DUPLICATE_EMAIL");
        }

        // 4. 비밀번호 재설정은 가입된 이메일만 가능
        if (purpose.equals("RESET_PW")
                && !userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("USER_NOT_FOUND");
        }

        LocalDateTime now = LocalDateTime.now();

        // 5. 가장 최근 인증 기록 조회
        EmailVerification previous =
                emailVerificationRepository
                        .findFirstByEmailAndPurposeOrderByCreatedAtDesc(
                                email,
                                purpose
                        )
                        .orElse(null);

        // 6. 60초 재전송 제한 검사
        if (previous != null) {
            LocalDateTime resendAvailableAt =
                    previous.getCreatedAt().plusSeconds(60);

            if (now.isBefore(resendAvailableAt)) {
                throw new IllegalArgumentException("RESEND_TOO_EARLY");
            }
        }

        // 7. 기존 PENDING 인증코드 무효화
        emailVerificationRepository
                .findFirstByEmailAndPurposeAndStatusOrderByCreatedAtDesc(
                        email,
                        purpose,
                        "PENDING"
                )
                .ifPresent(verification ->
                        verification.setStatus("INVALIDATED")
                );

        // 8. 6자리 인증번호 생성
        String code = generateVerificationCode();

        // 9. 만료시간 설정 (5분)
        LocalDateTime expiresAt = now.plusMinutes(5);

        // 10. 인증 정보 생성
        EmailVerification verification =
                new EmailVerification(
                        email,
                        purpose,
                        code,
                        expiresAt
                );

        // 11. DB 저장
        emailVerificationRepository.save(verification);

        // 이메일 발송
        emailService.sendVerificationEmail(
                email,
                code,
                purpose
        );

        // 12. 응답 생성
        LocalDateTime resendAvailableAt = now.plusSeconds(60);

        return new EmailVerificationSendResponse(
                expiresAt,
                resendAvailableAt
        );
    }

    // 6자리 숫자 인증코드 생성
    private String generateVerificationCode() {
        int code = secureRandom.nextInt(1000000);
        return String.format("%06d", code);
    }
}