package com.submate.submate.controller;

import com.submate.submate.dto.EmailVerificationSendRequest;
import com.submate.submate.dto.EmailVerificationSendResponse;
import com.submate.submate.service.EmailVerificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/email")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    public EmailVerificationController(
            EmailVerificationService emailVerificationService
    ) {
        this.emailVerificationService = emailVerificationService;
    }

    // 인증번호 발송 API
    @PostMapping("/send")
    public ResponseEntity<?> sendVerificationCode(
            @RequestBody EmailVerificationSendRequest request
    ) {

        try {
            EmailVerificationSendResponse response =
                    emailVerificationService.sendVerificationCode(
                            request.getEmail(),
                            request.getPurpose()
                    );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {

            String errorCode = e.getMessage();

            HttpStatus status;

            switch (errorCode) {
                case "DUPLICATE_EMAIL":
                    status = HttpStatus.CONFLICT;
                    break;

                case "USER_NOT_FOUND":
                    status = HttpStatus.NOT_FOUND;
                    break;

                case "RESEND_TOO_EARLY":
                    status = HttpStatus.TOO_MANY_REQUESTS;
                    break;

                default:
                    status = HttpStatus.BAD_REQUEST;
                    break;
            }

            return ResponseEntity
                    .status(status)
                    .body(Map.of("error", errorCode));
        }
    }
}