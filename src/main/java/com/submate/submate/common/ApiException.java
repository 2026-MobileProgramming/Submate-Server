package com.submate.submate.common;

public class ApiException extends RuntimeException {

    private final ApiErrorCode errorCode;
    private final Integer remainingAttempts;

    public ApiException(ApiErrorCode errorCode) {
        this(errorCode, errorCode.message(), null);
    }

    public ApiException(ApiErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    public static ApiException codeMismatch(int remainingAttempts) {
        return new ApiException(ApiErrorCode.CODE_MISMATCH, ApiErrorCode.CODE_MISMATCH.message(), remainingAttempts);
    }

    private ApiException(ApiErrorCode errorCode, String message, Integer remainingAttempts) {
        super(message);
        this.errorCode = errorCode;
        this.remainingAttempts = remainingAttempts;
    }

    public ApiErrorCode errorCode() {
        return errorCode;
    }

    public Integer remainingAttempts() {
        return remainingAttempts;
    }
}
