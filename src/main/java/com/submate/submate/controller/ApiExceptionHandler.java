package com.submate.submate.controller;

import com.submate.submate.common.ApiErrorCode;
import com.submate.submate.common.ApiException;
import com.submate.submate.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException exception) {
        ApiErrorCode error = exception.errorCode();
        return ResponseEntity.status(error.status()).body(
                new ErrorResponse(error.name(), exception.getMessage(), exception.remainingAttempts())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpectedException(Exception exception, WebRequest request) {
        return handleExceptionInternal(
                exception, null, HttpHeaders.EMPTY, HttpStatus.INTERNAL_SERVER_ERROR, request
        );
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception exception, Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request
    ) {
        ErrorResponse response;
        if (status.is5xxServerError()) {
            log.error("API request failed with HTTP status {}", status.value(), exception);
            response = defaultResponse(ApiErrorCode.INTERNAL_ERROR);
        } else if (status.value() == HttpStatus.BAD_REQUEST.value()) {
            response = defaultResponse(ApiErrorCode.INVALID_INPUT);
        } else {
            response = new ErrorResponse("HTTP_" + status.value(), "요청을 처리할 수 없습니다.");
        }

        return super.handleExceptionInternal(exception, response, headers, status, request);
    }

    private ErrorResponse defaultResponse(ApiErrorCode error) {
        return new ErrorResponse(error.name(), error.message());
    }
}
