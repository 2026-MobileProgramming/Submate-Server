package com.submate.submate.controller;

import com.submate.submate.dto.SignupRequest;
import com.submate.submate.dto.LoginRequest;
import com.submate.submate.dto.LoginResponse;
import com.submate.submate.dto.PasswordResetRequest;
import com.submate.submate.entity.User;
import com.submate.submate.service.AuthService;

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
    public ResponseEntity<Map<String, Long>> signup(
            @RequestBody SignupRequest request
    ) {
        Long userId = authService.signup(request);

        return ResponseEntity.status(201).body(
                Map.of("userId", userId)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request
    ) {
        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Void> resetPassword(
            @RequestBody PasswordResetRequest request
    ) {
        authService.resetPassword(request);

        return ResponseEntity.noContent().build();
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
