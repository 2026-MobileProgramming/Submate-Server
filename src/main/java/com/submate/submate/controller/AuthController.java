
package com.submate.submate.controller;

import com.submate.submate.dto.SignupRequest;
import com.submate.submate.service.AuthService;
import com.submate.submate.dto.LoginRequest;
import com.submate.submate.dto.LoginResponse;

import org.springframework.http.HttpStatus;
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
}
