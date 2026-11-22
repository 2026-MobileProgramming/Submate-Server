package com.submate.submate.dto;

import com.submate.submate.entity.Subscription;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SubscriptionResponse {

    private Long id;
    private String category;
    private String serviceName;
    private String status;
    private String billingCycle;
    private int paymentMonth;
    private int paymentDay;
    private String currency;
    private BigDecimal amount;
    private String memo;
    private String packageName;
    private LocalDate startDate;
    private LocalDate nextPaymentDate;
    private LocalDate trialEndDate;
    private LocalDate cancelDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SubscriptionResponse(Subscription subscription) {

        this.id = subscription.getId();
        this.category = subscription.getCategory().getName();
        this.serviceName = subscription.getServiceName();
        this.status = subscription.getStatus();

        // 무료체험인지 유료 구독인지 구분
        boolean isTrial = "TRIAL".equals(subscription.getPlanType());

        this.billingCycle = isTrial ? null : subscription.getBillingCycle();

        // 결제 월
        if (isTrial) {
            this.paymentMonth = subscription.getTrialEndDate() != null
                    ? subscription.getTrialEndDate().getMonthValue()
                    : 0;
        } else if ("YEARLY".equals(subscription.getBillingCycle())) {
            this.paymentMonth = subscription.getStartDate() != null
                    ? subscription.getStartDate().getMonthValue()
                    : 0;
        } else {
            this.paymentMonth = 0;
        }

        // 결제일 또는 무료체험 종료일
        this.paymentDay = isTrial
                ? (subscription.getTrialEndDate() != null
                ? subscription.getTrialEndDate().getDayOfMonth()
                : 0)
                : (subscription.getBillingDay() != null
                ? subscription.getBillingDay()
                : 0);

        // 무료체험은 앱에서 FREE_TRIAL로 표시
        this.currency = isTrial ? "FREE_TRIAL" : subscription.getCurrency();

        this.amount = isTrial
                ? BigDecimal.ZERO
                : subscription.getAmount();

        this.memo = subscription.getMemo() != null
                ? subscription.getMemo()
                : "";

        this.packageName = subscription.getPackageName() != null
                ? subscription.getPackageName()
                : "";

        this.startDate = isTrial ? null : subscription.getStartDate();

        this.trialEndDate = isTrial
                ? subscription.getTrialEndDate()
                : null;

        this.cancelDate = subscription.getCancelDate();

        this.createdAt = subscription.getCreatedAt();
        this.updatedAt = subscription.getUpdatedAt();

        // 다음 결제일 계산
        this.nextPaymentDate = calculateNextPaymentDate(subscription);
    }

    private LocalDate calculateNextPaymentDate(Subscription subscription) {

        // 무료체험 또는 해지완료된 구독은 다음 결제일이 없음
        if ("TRIAL".equals(subscription.getPlanType())
                || "CANCELED".equals(subscription.getStatus())
                || subscription.getStartDate() == null) {
            return null;
        }

        LocalDate today = LocalDate.now();
        LocalDate paymentDate = subscription.getStartDate();

        // 시작일이 오늘보다 이전이라면 다음 결제일을 계산
        while (paymentDate.isBefore(today)) {

            if ("YEARLY".equals(subscription.getBillingCycle())) {
                paymentDate = paymentDate.plusYears(1);
            } else {
                paymentDate = paymentDate.plusMonths(1);

                // 매월 결제일 기준으로 계산 (31일 등 말일 처리)
                int billingDay = subscription.getBillingDay();

                int lastDay = paymentDate.lengthOfMonth();

                paymentDate = paymentDate.withDayOfMonth(
                        Math.min(billingDay, lastDay)
                );
            }
        }

        return paymentDate;
    }

    public Long getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getStatus() {
        return status;
    }

    public String getBillingCycle() {
        return billingCycle;
    }

    public int getPaymentMonth() {
        return paymentMonth;
    }

    public int getPaymentDay() {
        return paymentDay;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getMemo() {
        return memo;
    }

    public String getPackageName() {
        return packageName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getNextPaymentDate() {
        return nextPaymentDate;
    }

    public LocalDate getTrialEndDate() {
        return trialEndDate;
    }

    public LocalDate getCancelDate() {
        return cancelDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}