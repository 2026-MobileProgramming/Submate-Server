package com.submate.submate.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // 직접 입력한 서비스면 null
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private ServiceItem service;

    @Column(name = "service_name", nullable = false, length = 50)
    private String serviceName;

    // ACTIVE, CANCEL_SCHEDULED, CANCELED
    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    // PAID, TRIAL
    @Column(name = "plan_type", nullable = false, length = 10)
    private String planType;

    // KRW, JPY, USD (TRIAL 이면 null)
    @Column(columnDefinition = "CHAR(3)")
    private String currency;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    // MONTHLY, YEARLY (TRIAL 이면 null)
    @Column(name = "billing_cycle", length = 10)
    private String billingCycle;

    @Column(name = "billing_day")
    private Integer billingDay;

    // 첫 결제일
    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "trial_end_date")
    private LocalDate trialEndDate;

    @Column(name = "cancel_date")
    private LocalDate cancelDate;

    @Column(length = 500)
    private String memo;

    @Column(name = "package_name", length = 150)
    private String packageName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Subscription() {
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Category getCategory() {
        return category;
    }

    public ServiceItem getService() {
        return service;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getStatus() {
        return status;
    }

    public String getPlanType() {
        return planType;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getBillingCycle() {
        return billingCycle;
    }

    public Integer getBillingDay() {
        return billingDay;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getTrialEndDate() {
        return trialEndDate;
    }

    public LocalDate getCancelDate() {
        return cancelDate;
    }

    public String getMemo() {
        return memo;
    }

    public String getPackageName() {
        return packageName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
