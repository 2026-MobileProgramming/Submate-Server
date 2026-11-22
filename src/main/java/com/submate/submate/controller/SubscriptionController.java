package com.submate.submate.controller;

import com.submate.submate.dto.SubscriptionCreateRequest;
import com.submate.submate.dto.SubscriptionResponse;
import com.submate.submate.dto.SubscriptionUpdateRequest;
import com.submate.submate.entity.User;
import com.submate.submate.exception.SubscriptionNotFoundException;
import com.submate.submate.service.SubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    // 내 구독 목록 조회
    @GetMapping
    public List<SubscriptionResponse> getMySubscriptions(
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        return subscriptionService.getMySubscriptions(user.getId());
    }

    // 구독 등록
    @PostMapping
    public ResponseEntity<?> createSubscription(
            Authentication authentication,
            @RequestBody SubscriptionCreateRequest request
    ) {
        User user = (User) authentication.getPrincipal();

        try {
            SubscriptionResponse response = subscriptionService.createSubscription(user, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return invalidInput(e);
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSubscription(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody SubscriptionUpdateRequest request
    ) {
        User user = (User) authentication.getPrincipal();

        try {
            return ResponseEntity.ok(subscriptionService.updateSubscription(
                    user.getId(),
                    id,
                    request
            ));
        } catch (SubscriptionNotFoundException e) {
            return notFound(e);
        } catch (IllegalArgumentException e) {
            return invalidInput(e);
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubscription(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = (User) authentication.getPrincipal();

        try {
            subscriptionService.deleteSubscription(
                    user.getId(),
                    id
            );
            return ResponseEntity.noContent().build();
        } catch (SubscriptionNotFoundException e) {
            return notFound(e);
        }
    }

    private ResponseEntity<Map<String, String>> invalidInput(IllegalArgumentException e) {
        String message = e.getMessage() != null ? e.getMessage() : "입력값이 올바르지 않습니다.";
        return ResponseEntity.badRequest().body(
                Map.of("code", "INVALID_INPUT", "message", message)
        );
    }

    private ResponseEntity<Map<String, String>> notFound(SubscriptionNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of("code", "SUBSCRIPTION_NOT_FOUND", "message", e.getMessage())
        );
    }
}
