package com.submate.submate.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "service_name", nullable = false, length = 50)
    private String serviceName;

    @Column(name = "paid_date", nullable = false)
    private LocalDate paidDate;

    @Column(name = "billing_cycle", nullable = false, length = 10)
    private String billingCycle;

    @Column(nullable = false, columnDefinition = "CHAR(3)")
    private String currency;

    // 원래 통화 금액
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    // 1 외화당 원화 (KRW 는 1)
    @Column(name = "exchange_rate", nullable = false, precision = 18, scale = 6)
    private BigDecimal exchangeRate;

    @Column(name = "rate_date")
    private LocalDate rateDate;

    // 원화 환산 금액 (집계는 이 컬럼만 사용)
    @Column(name = "amount_krw", nullable = false, precision = 14, scale = 0)
    private BigDecimal amountKrw;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Payment() {
    }

    public Payment(Subscription subscription, LocalDate paidDate, BigDecimal exchangeRate,
                   LocalDate rateDate, BigDecimal amountKrw) {
        this.subscription = subscription;
        this.user = subscription.getUser();
        this.serviceName = subscription.getServiceName();
        this.paidDate = paidDate;
        this.billingCycle = subscription.getBillingCycle();
        this.currency = subscription.getCurrency();
        this.amount = subscription.getAmount();
        this.exchangeRate = exchangeRate;
        this.rateDate = rateDate;
        this.amountKrw = amountKrw;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public User getUser() {
        return user;
    }

    public String getServiceName() {
        return serviceName;
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

    public LocalDate getRateDate() {
        return rateDate;
    }

    public BigDecimal getAmountKrw() {
        return amountKrw;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
