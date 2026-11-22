package com.submate.submate.service;

import com.submate.submate.entity.ExchangeRate;
import com.submate.submate.entity.ExchangeRateId;
import com.submate.submate.repository.ExchangeRateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// Frankfurter API에서 환율을 받아 EXCHANGE_RATES에 저장
// 실패하면 저장하지 않고 넘어감 (기존 최신 환율을 계속 사용)
@Service
public class ExchangeRateService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateService.class);

    // 앱과 같은 Frankfurter 주소 사용
    private static final String FRANKFURTER_URL = "https://api.frankfurter.dev/v1/latest?base={base}&symbols=KRW";
    private static final List<String> CURRENCIES = List.of("USD", "JPY");

    private final ExchangeRateRepository exchangeRateRepository;
    private final RestClient restClient = RestClient.create();

    public ExchangeRateService(ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
    }

    // USD, JPY 환율을 받아서 저장
    public void updateRates() {
        for (String currency : CURRENCIES) {
            try {
                FrankfurterResponse response = restClient.get()
                        .uri(FRANKFURTER_URL, currency)
                        .retrieve()
                        .body(FrankfurterResponse.class);

                if (response == null || response.rates() == null || response.rates().get("KRW") == null) {
                    log.warn("환율 응답에 KRW 값이 없습니다. currency={}", currency);
                    continue;
                }

                save(LocalDate.parse(response.date()), currency, response.rates().get("KRW"));
                log.info("환율 저장 완료: {} = {} KRW ({})", currency, response.rates().get("KRW"), response.date());
            } catch (Exception e) {
                log.warn("환율 갱신 실패, 기존 환율 사용: currency={}, error={}", currency, e.getMessage());
            }
        }
    }

    // 같은 날짜·통화가 이미 있으면 값만 갱신
    public void save(LocalDate rateDate, String currency, BigDecimal rateToKrw) {
        ExchangeRate rate = exchangeRateRepository.findById(new ExchangeRateId(rateDate, currency))
                .orElse(null);

        if (rate == null) {
            rate = new ExchangeRate(rateDate, currency, rateToKrw);
        } else {
            rate.updateRate(rateToKrw);
        }
        exchangeRateRepository.save(rate);
    }

    // Frankfurter 응답 형식: {"amount":1.0,"base":"USD","date":"2026-11-20","rates":{"KRW":1385.4}}
    public record FrankfurterResponse(BigDecimal amount, String base, String date, Map<String, BigDecimal> rates) {
    }
}
