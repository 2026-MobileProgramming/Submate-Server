package com.submate.submate.dto;

import com.submate.submate.common.CategoryCode;
import com.submate.submate.entity.Payment;
import com.submate.submate.entity.Subscription;

import java.math.BigDecimal;
import java.time.LocalDate;

// GET /api/payments 응답 항목 (금액·환율은 결제 당시 값)
public class PaymentResponse {

    private final Long id;
    private final Long subscriptionId;
    private final String serviceName;
    private final String category;
    private final LocalDate paidDate;
    private final String billingCycle;
    private final String currency;
    private final BigDecimal amount;
    private final BigDecimal exchangeRate;
    private final BigDecimal amountKrw;

    private PaymentResponse(Payment payment) {
        Subscription subscription = payment.getSubscription();
        this.id = payment.getId();
        this.subscriptionId = subscription.getId();
        this.serviceName = subscription.getServiceName();
        this.category = CategoryCode.fromId(subscription.getCategory().getId()).name();
        this.paidDate = payment.getPaidDate();
        this.billingCycle = payment.getBillingCycle();
        this.currency = payment.getCurrency();
        this.amount = payment.getAmount();
        this.exchangeRate = payment.getExchangeRate();
        this.amountKrw = payment.getAmountKrw();
    }

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(payment);
    }

    public Long getId() {
        return id;
    }

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getCategory() {
        return category;
    }

    public LocalDate getPaidDate() {
        return paidDate;
    }

    public String getBillingCycle() {
        return billingCycle;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public BigDecimal getAmountKrw() {
        return amountKrw;
    }
}
