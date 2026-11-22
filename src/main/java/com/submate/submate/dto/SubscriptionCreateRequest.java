package com.submate.submate.dto;

import java.math.BigDecimal;

public class SubscriptionCreateRequest {

    private String category;
    private String serviceName;
    private String status;
    private String planType;
    private String currency;
    private BigDecimal amount;
    private String billingCycle;
    private Integer billingDay;
    private Integer paymentMonth;
    private Integer paymentDay;
    private String memo;
    private String packageName;

    public String getCategory() {
        return category;
    }

    public String getServiceName() {
        return serviceName;
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

    public Integer getPaymentMonth() {
        return paymentMonth;
    }

    public Integer getPaymentDay() {
        return paymentDay;
    }

    public String getMemo() {
        return memo;
    }

    public String getPackageName() {
        return packageName;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setBillingCycle(String billingCycle) {
        this.billingCycle = billingCycle;
    }

    public void setBillingDay(Integer billingDay) {
        this.billingDay = billingDay;
    }

    public void setPaymentMonth(Integer paymentMonth) {
        this.paymentMonth = paymentMonth;
    }

    public void setPaymentDay(Integer paymentDay) {
        this.paymentDay = paymentDay;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
