package com.submate.submate.scheduler;

import com.submate.submate.service.ExchangeRateService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// 매일 00:05(한국 시간) 실행
// 순서: 1. 환율 저장 → 2. 상태 자동 전환 → 3. 결제 기록 (2, 3은 구독 API 이후 추가 예정)
@Component
public class DailyScheduler {

    private final ExchangeRateService exchangeRateService;

    public DailyScheduler(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul")
    public void runDaily() {
        exchangeRateService.updateRates();
    }

    // 서버가 켜질 때도 한 번 환율을 받아 둠 (재배포 직후에도 최신 환율 사용)
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        exchangeRateService.updateRates();
    }
}
