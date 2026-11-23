package com.submate.submate.exception;

import com.submate.submate.common.ApiErrorCode;
import com.submate.submate.common.ApiException;
// 없는 구독이거나 남의 구독일 때 (404 SUBSCRIPTION_NOT_FOUND)
public class SubscriptionNotFoundException extends ApiException {

    public SubscriptionNotFoundException() {
        super(ApiErrorCode.SUBSCRIPTION_NOT_FOUND);
    }
}
