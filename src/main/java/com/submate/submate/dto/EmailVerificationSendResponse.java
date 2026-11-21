package com.submate.submate.dto;

import java.time.LocalDateTime;

public class EmailVerificationSendResponse {

    private LocalDateTime expiresAt;
    private LocalDateTime resendAvailableAt;

    public EmailVerificationSendResponse(
            LocalDateTime expiresAt,
            LocalDateTime resendAvailableAt
    ) {
        this.expiresAt = expiresAt;
        this.resendAvailableAt = resendAvailableAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getResendAvailableAt() {
        return resendAvailableAt;
    }
}