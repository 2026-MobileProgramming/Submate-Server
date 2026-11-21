package com.submate.submate.repository;

import com.submate.submate.entity.ExchangeRate;
import com.submate.submate.entity.ExchangeRateId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, ExchangeRateId> {

    // 통화별 가장 최근 환율
    Optional<ExchangeRate> findTopByCurrencyOrderByRateDateDesc(String currency);
}
