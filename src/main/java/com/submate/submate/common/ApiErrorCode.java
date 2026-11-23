package com.submate.submate.common;

import org.springframework.http.HttpStatus;

public enum ApiErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "요청 값을 확인해 주세요."),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "이메일 인증을 완료해 주세요."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호와 비밀번호 확인이 일치하지 않습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호 규칙을 확인해 주세요."),
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "닉네임 규칙을 확인해 주세요."),
    INVALID_EMAIL(HttpStatus.BAD_REQUEST, "이메일 형식을 확인해 주세요."),
    INVALID_PURPOSE(HttpStatus.BAD_REQUEST, "이메일 인증 목적을 확인해 주세요."),
    INVALID_CODE_FORMAT(HttpStatus.BAD_REQUEST, "인증번호 형식을 확인해 주세요."),
    CODE_MISMATCH(HttpStatus.BAD_REQUEST, "인증번호가 일치하지 않습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "인증번호를 찾을 수 없습니다."),
    SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "구독을 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    CODE_EXPIRED(HttpStatus.GONE, "인증번호가 만료되었습니다."),
    RESEND_TOO_EARLY(HttpStatus.TOO_MANY_REQUESTS, "잠시 후 인증번호를 다시 요청해 주세요."),
    TOO_MANY_ATTEMPTS(HttpStatus.TOO_MANY_REQUESTS, "인증 시도 횟수를 초과했습니다. 새 인증번호를 요청해 주세요."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");

    private final HttpStatus status;
    private final String message;

    ApiErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus status() {
        return status;
    }

    public String message() {
        return message;
    }
}
