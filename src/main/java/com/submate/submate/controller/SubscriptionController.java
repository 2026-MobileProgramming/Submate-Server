package com.submate.submate.controller;

import com.submate.submate.dto.SubscriptionCreateRequest;
import com.submate.submate.dto.SubscriptionResponse;
import com.submate.submate.dto.SubscriptionUpdateRequest;
import com.submate.submate.entity.User;
import com.submate.submate.service.SubscriptionService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public SubscriptionResponse createSubscription(
            Authentication authentication,
            @RequestBody SubscriptionCreateRequest request
    ) {
        User user = (User) authentication.getPrincipal();

        return subscriptionService.createSubscription(user, request);
    }
    @PutMapping("/{id}")
    public SubscriptionResponse updateSubscription(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody SubscriptionUpdateRequest request
    ) {
        User user = (User) authentication.getPrincipal();

        return subscriptionService.updateSubscription(
                user.getId(),
                id,
                request
        );
    }
    @DeleteMapping("/{id}")
    public void deleteSubscription(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = (User) authentication.getPrincipal();

        subscriptionService.deleteSubscription(
                user.getId(),
                id
        );
    }
}