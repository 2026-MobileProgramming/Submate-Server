package com.submate.submate.controller;

import com.submate.submate.dto.EmailVerificationSendRequest;
import com.submate.submate.dto.EmailVerificationSendResponse;
import com.submate.submate.dto.EmailVerificationVerifyRequest;
import com.submate.submate.dto.EmailVerificationVerifyResponse;
import com.submate.submate.service.EmailVerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<EmailVerificationSendResponse> sendVerificationCode(
            @RequestBody EmailVerificationSendRequest request
    ) {

        EmailVerificationSendResponse response =
                emailVerificationService.sendVerificationCode(
                        request.getEmail(),
                        request.getPurpose()
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<EmailVerificationVerifyResponse> verifyCode(
            @RequestBody EmailVerificationVerifyRequest request
    ) {
        EmailVerificationVerifyResponse response =
                emailVerificationService.verifyCode(
                        request.getEmail(),
                        request.getPurpose(),
                        request.getCode()
                );

        return ResponseEntity.ok(response);
    }

}
