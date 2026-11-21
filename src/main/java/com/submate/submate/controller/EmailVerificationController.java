package com.submate.submate.controller;

import com.submate.submate.dto.EmailVerificationSendRequest;
import com.submate.submate.dto.EmailVerificationSendResponse;
import com.submate.submate.dto.EmailVerificationVerifyRequest;
import com.submate.submate.dto.EmailVerificationVerifyResponse;
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
    @PostMapping("/verify")
    public ResponseEntity<?> verifyCode(
            @RequestBody EmailVerificationVerifyRequest request
    ) {
        try {
            EmailVerificationVerifyResponse response =
                    emailVerificationService.verifyCode(
                            request.getEmail(),
                            request.getPurpose(),
                            request.getCode()
                    );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {

            String error = e.getMessage();

            if (error.startsWith("CODE_MISMATCH")) {
                int remainingAttempts = 0;

                if (error.contains(":")) {
                    remainingAttempts = Integer.parseInt(
                            error.split(":")[1]
                    );
                }

                return ResponseEntity.badRequest().body(
                        Map.of(
                                "error", "CODE_MISMATCH",
                                "remainingAttempts", remainingAttempts
                        )
                );
            }

            if (error.equals("CODE_EXPIRED")) {
                return ResponseEntity.status(410).body(
                        Map.of("error", "CODE_EXPIRED")
                );
            }

            if (error.equals("TOO_MANY_ATTEMPTS")) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "TOO_MANY_ATTEMPTS")
                );
            }

            if (error.equals("CODE_NOT_FOUND")) {
                return ResponseEntity.status(404).body(
                        Map.of("error", "CODE_NOT_FOUND")
                );
            }

            return ResponseEntity.badRequest().body(
                    Map.of("error", error)
            );
        }
    }

}