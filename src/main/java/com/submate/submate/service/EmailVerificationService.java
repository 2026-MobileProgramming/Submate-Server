package com.submate.submate.service;

import com.submate.submate.common.ApiErrorCode;
import com.submate.submate.common.ApiException;
import com.submate.submate.dto.EmailVerificationSendResponse;
import com.submate.submate.entity.EmailVerification;
import com.submate.submate.repository.EmailVerificationRepository;
import com.submate.submate.repository.UserRepository;
import com.submate.submate.dto.EmailVerificationVerifyRequest;
import com.submate.submate.dto.EmailVerificationVerifyResponse;

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
    @Transactional(noRollbackFor = ApiException.class)
    public EmailVerificationSendResponse sendVerificationCode(
            String email,
            String purpose
    ) {

        // 1. 이메일 입력값 검사
        if (email == null || email.isBlank()) {
            throw new ApiException(ApiErrorCode.INVALID_EMAIL);
        }

        email = email.trim().toLowerCase();

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new ApiException(ApiErrorCode.INVALID_EMAIL);
        }

        // 2. 인증 목적 검사
        if (purpose == null ||
                (!purpose.equals("SIGNUP") && !purpose.equals("RESET_PW"))) {
            throw new ApiException(ApiErrorCode.INVALID_PURPOSE);
        }

        // 3. 회원가입 이메일 중복 검사
        if (purpose.equals("SIGNUP")
                && userRepository.existsByEmail(email)) {
            throw new ApiException(ApiErrorCode.DUPLICATE_EMAIL);
        }

        // 4. 비밀번호 재설정은 가입된 이메일만 가능
        if (purpose.equals("RESET_PW")
                && !userRepository.existsByEmail(email)) {
            throw new ApiException(ApiErrorCode.USER_NOT_FOUND);
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
                throw new ApiException(ApiErrorCode.RESEND_TOO_EARLY);
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

    @Transactional(noRollbackFor = ApiException.class)
    public EmailVerificationVerifyResponse verifyCode(
            String email,
            String purpose,
            String code
    ) {

        // 1. 이메일 입력값 검사
        if (email == null || email.isBlank()) {
            throw new ApiException(ApiErrorCode.INVALID_EMAIL);
        }

        email = email.trim().toLowerCase();

        // 2. 인증 목적 검사
        if (purpose == null ||
                (!purpose.equals("SIGNUP") && !purpose.equals("RESET_PW"))) {
            throw new ApiException(ApiErrorCode.INVALID_PURPOSE);
        }

        // 3. 인증번호 입력값 검사
        if (code == null || !code.matches("\\d{6}")) {
            throw new ApiException(ApiErrorCode.INVALID_CODE_FORMAT);
        }

        // 4. 해당 이메일과 목적의 PENDING 인증번호 조회
        EmailVerification verification =
                emailVerificationRepository
                        .findFirstByEmailAndPurposeOrderByCreatedAtDesc(
                                email,
                                purpose
                        )
                        .orElseThrow(() ->
                                new ApiException(ApiErrorCode.CODE_NOT_FOUND)
                        );
        // 인증번호가 이미 무효화된 경우
        if (verification.getStatus().equals("INVALIDATED")) {

            if (verification.getFailCount() >= 5) {
                throw new ApiException(ApiErrorCode.TOO_MANY_ATTEMPTS);
            }

            throw new ApiException(ApiErrorCode.CODE_NOT_FOUND);
        }

        // 이미 인증 완료된 경우
        if (verification.getStatus().equals("VERIFIED")) {
            throw new ApiException(ApiErrorCode.CODE_NOT_FOUND);
        }

        // 5. 이미 5회 실패했는지 확인
        if (verification.getFailCount() >= 5) {
            verification.setStatus("INVALIDATED");
            throw new ApiException(ApiErrorCode.TOO_MANY_ATTEMPTS);
        }

        // 6. 인증번호 만료 여부 확인
        if (LocalDateTime.now().isAfter(verification.getExpiresAt())) {
            verification.setStatus("INVALIDATED");
            throw new ApiException(ApiErrorCode.CODE_EXPIRED);
        }

        // 7. 입력한 인증번호와 저장된 인증번호 비교
        if (!verification.getCode().equals(code)) {

            int failCount = verification.getFailCount() + 1;

            verification.setFailCount(failCount);

            if (failCount >= 5) {
                verification.setStatus("INVALIDATED");
            }

            throw ApiException.codeMismatch(Math.max(0, 5 - failCount));
        }

        // 8. 인증 성공 처리
        verification.setStatus("VERIFIED");
        verification.setVerifiedAt(LocalDateTime.now());

        return new EmailVerificationVerifyResponse(true);
    }
}