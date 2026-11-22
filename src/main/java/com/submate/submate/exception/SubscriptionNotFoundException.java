package com.submate.submate.exception;

// 없는 구독이거나 남의 구독일 때 (404 SUBSCRIPTION_NOT_FOUND)
public class SubscriptionNotFoundException extends RuntimeException {

    public SubscriptionNotFoundException() {
        super("구독을 찾을 수 없습니다.");
    }
}
