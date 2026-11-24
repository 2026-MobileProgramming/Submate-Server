package com.submate.submate.scheduler;

import com.submate.submate.service.ExchangeRateService;
import com.submate.submate.service.PaymentRecordingService;
import com.submate.submate.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

// 매일 00:05(한국 시간) 실행
@Component
public class DailyScheduler {

    private static final Logger log = LoggerFactory.getLogger(DailyScheduler.class);

    private final ExchangeRateService exchangeRateService;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRecordingService paymentRecordingService;

    public DailyScheduler(ExchangeRateService exchangeRateService, SubscriptionRepository subscriptionRepository,
                          PaymentRecordingService paymentRecordingService) {
        this.exchangeRateService = exchangeRateService;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentRecordingService = paymentRecordingService;
    }

    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul")
    public synchronized void runDaily() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        exchangeRateService.updateRates();
        subscriptionRepository.cancelExpired(now.toLocalDate(), now);
        paymentRecordingService.recordThrough(now.toLocalDate());
    }

    // 서버가 켜질 때도 한 번 환율을 받아 둠 (재배포 직후에도 최신 환율 사용)
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        try {
            runDaily();
        } catch (RuntimeException e) {
            log.error("Daily processing failed on startup and will be retried at the next scheduled run", e);
        }
    }
}
