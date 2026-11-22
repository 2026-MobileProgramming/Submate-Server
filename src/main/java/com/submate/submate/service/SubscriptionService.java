package com.submate.submate.service;

import com.submate.submate.dto.SubscriptionResponse;
import com.submate.submate.entity.Subscription;
import com.submate.submate.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    // 내 구독 목록 조회
    public List<SubscriptionResponse> getMySubscriptions(Long userId) {

        List<Subscription> subscriptions =
                subscriptionRepository.findByUserId(userId);

        return subscriptions.stream()
                .map(SubscriptionResponse::new)
                .toList();
    }
}