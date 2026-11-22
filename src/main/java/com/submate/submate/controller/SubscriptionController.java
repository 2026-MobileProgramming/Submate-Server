package com.submate.submate.controller;

import com.submate.submate.dto.SubscriptionResponse;
import com.submate.submate.entity.User;
import com.submate.submate.service.SubscriptionService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

        // 현재 로그인한 사용자 정보 가져오기
        User user = (User) authentication.getPrincipal();

        // 사용자 ID로 구독 목록 조회
        return subscriptionService.getMySubscriptions(user.getId());
    }
}