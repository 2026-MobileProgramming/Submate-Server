
package com.submate.submate.controller;

import com.submate.submate.dto.SignupRequest;
import com.submate.submate.dto.LoginRequest;
import com.submate.submate.dto.LoginResponse;
import com.submate.submate.dto.PasswordResetRequest;
import com.submate.submate.entity.User;
import com.submate.submate.service.AuthService;


import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @RequestBody SignupRequest request
    ) {
        try {
            Long userId = authService.signup(request);

            return ResponseEntity.status(201).body(
                    Map.of("userId", userId)
            );

        } catch (IllegalArgumentException e) {

            String error = e.getMessage();

            if ("EMAIL_NOT_VERIFIED".equals(error)) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "EMAIL_NOT_VERIFIED")
                );
            }

            if ("DUPLICATE_EMAIL".equals(error)) {
                return ResponseEntity.status(409).body(
                        Map.of("error", "DUPLICATE_EMAIL")
                );
            }

            if ("PASSWORD_MISMATCH".equals(error)) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "PASSWORD_MISMATCH")
                );
            }

            if ("INVALID_PASSWORD".equals(error)) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "INVALID_PASSWORD")
                );
            }

            if ("INVALID_NICKNAME".equals(error)) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "INVALID_NICKNAME")
                );
            }

            return ResponseEntity.badRequest().body(
                    Map.of("error", error)
            );
        }
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request
    ) {
        try {
            LoginResponse response = authService.login(request);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.status(401).body(
                    Map.of("error", "INVALID_CREDENTIALS")
            );
        }
    }
    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(
            @RequestBody PasswordResetRequest request
    ) {
        try {
            authService.resetPassword(request);

            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {

            String errorCode = e.getMessage();

            if ("EMAIL_NOT_VERIFIED".equals(errorCode)) {
                return ResponseEntity.badRequest().body(
                        Map.of("code", "EMAIL_NOT_VERIFIED")
                );
            }

            if ("PASSWORD_MISMATCH".equals(errorCode)) {
                return ResponseEntity.badRequest().body(
                        Map.of("code", "PASSWORD_MISMATCH")
                );
            }

            if ("INVALID_PASSWORD".equals(errorCode)) {
                return ResponseEntity.badRequest().body(
                        Map.of("code", "INVALID_INPUT")
                );
            }

            if ("USER_NOT_FOUND".equals(errorCode)) {
                return ResponseEntity.status(404).body(
                        Map.of("code", "USER_NOT_FOUND")
                );
            }

            return ResponseEntity.badRequest().body(
                    Map.of("code", "INVALID_INPUT")
            );
        }
    }
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                new LoginResponse.UserInfo(
                        user.getId(),
                        user.getEmail(),
                        user.getNickname()
                )
        );
    }
}
