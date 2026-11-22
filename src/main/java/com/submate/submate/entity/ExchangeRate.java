package com.submate.submate.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_rates")
@IdClass(ExchangeRateId.class)
public class ExchangeRate {

    @Id
    @Column(name = "rate_date")
    private LocalDate rateDate;

    // USD, JPY
    @Id
    @Column(columnDefinition = "CHAR(3)")
    private String currency;

    // 1 외화당 원화
    @Column(name = "rate_to_krw", nullable = false, precision = 18, scale = 6)
    private BigDecimal rateToKrw;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    protected ExchangeRate() {
    }

    public ExchangeRate(LocalDate rateDate, String currency, BigDecimal rateToKrw) {
        this.rateDate = rateDate;
        this.currency = currency;
        this.rateToKrw = rateToKrw;
    }

    // 같은 날짜 환율을 다시 받았을 때 값 갱신
    public void updateRate(BigDecimal rateToKrw) {
        this.rateToKrw = rateToKrw;
        this.fetchedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (fetchedAt == null) {
            fetchedAt = LocalDateTime.now();
        }
    }

    public LocalDate getRateDate() {
        return rateDate;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getRateToKrw() {
        return rateToKrw;
    }

    public LocalDateTime getFetchedAt() {
        return fetchedAt;
    }
}
