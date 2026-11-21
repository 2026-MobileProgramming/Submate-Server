package com.submate.submate.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

// exchange_rates 테이블의 복합키 (rate_date, currency)
public class ExchangeRateId implements Serializable {

    private LocalDate rateDate;
    private String currency;

    public ExchangeRateId() {
    }

    public ExchangeRateId(LocalDate rateDate, String currency) {
        this.rateDate = rateDate;
        this.currency = currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExchangeRateId that)) return false;
        return Objects.equals(rateDate, that.rateDate)
                && Objects.equals(currency, that.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rateDate, currency);
    }
}
