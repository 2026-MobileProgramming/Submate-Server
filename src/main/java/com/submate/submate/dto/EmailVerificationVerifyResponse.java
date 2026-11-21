package com.submate.submate.dto;

public class EmailVerificationVerifyResponse {

    private boolean verified;

    public EmailVerificationVerifyResponse(boolean verified) {
        this.verified = verified;
    }

    public boolean isVerified() {
        return verified;
    }
}