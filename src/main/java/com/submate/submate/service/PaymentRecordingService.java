package com.submate.submate.service;

import com.submate.submate.common.PaymentDateCalculator;
import com.submate.submate.entity.ExchangeRate;
import com.submate.submate.entity.Payment;
import com.submate.submate.entity.Subscription;
import com.submate.submate.repository.ExchangeRateRepository;
import com.submate.submate.repository.PaymentRepository;
import com.submate.submate.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Service
public class PaymentRecordingService {

    private static final Logger log = LoggerFactory.getLogger(PaymentRecordingService.class);

    private final SubscriptionRepository subscriptions;
    private final PaymentRepository payments;
    private final ExchangeRateRepository exchangeRates;
    private final TransactionTemplate transaction;

    public PaymentRecordingService(SubscriptionRepository subscriptions, PaymentRepository payments,
                                   ExchangeRateRepository exchangeRates, TransactionTemplate transaction) {
        this.subscriptions = subscriptions;
        this.payments = payments;
        this.exchangeRates = exchangeRates;
        this.transaction = transaction;
    }

    public void recordThrough(LocalDate today) {
        for (Long id : subscriptions.findPaymentCandidateIds(today)) {
            try {
                transaction.executeWithoutResult(status -> subscriptions.findForPaymentRecording(id)
                        .ifPresent(subscription -> recordMissing(subscription, today)));
            } catch (RuntimeException e) {
                log.error("Payment recording failed for subscription {}", id, e);
            }
        }
    }

    public void recordFirstPaymentIfDue(Subscription subscription, LocalDate today) {
        LocalDate cancelDate = subscription.getCancelDate();
        if ("PAID".equals(subscription.getPlanType())
                && today.equals(subscription.getStartDate())
                && !"CANCELED".equals(subscription.getStatus())
                && (cancelDate == null || today.isBefore(cancelDate))) {
            recordPayment(subscription, today);
        }
    }

    private void recordMissing(Subscription subscription, LocalDate today) {
        LocalDate cancelDate = subscription.getCancelDate();
        if (!"PAID".equals(subscription.getPlanType())
                || ("CANCELED".equals(subscription.getStatus()) && cancelDate == null)) {
            return;
        }

        LocalDate startDate = subscription.getStartDate();
        LocalDate through = cancelDate != null && !cancelDate.isAfter(today)
                ? cancelDate.minusDays(1) : today;
        if (through.isBefore(startDate)) {
            return;
        }

        Set<LocalDate> recorded = new HashSet<>(
                payments.findRecordedDates(subscription.getId(), startDate, through));
        for (LocalDate date : PaymentDateCalculator.paymentDatesBetween(
                subscription.getBillingCycle(), startDate, subscription.getBillingDay(), startDate, through)) {
            if (!recorded.contains(date)) {
                recordPayment(subscription, date);
            }
        }
    }

    private void recordPayment(Subscription subscription, LocalDate date) {
        String currency = subscription.getCurrency();
        if ("KRW".equals(currency)) {
            payments.save(new Payment(subscription, date, BigDecimal.ONE, null, subscription.getAmount()));
            return;
        }

        ExchangeRate rate = exchangeRates.findTopByCurrencyAndRateDateLessThanEqualOrderByRateDateDesc(currency, date)
                .orElse(null);
        if (rate == null) {
            log.warn("Payment pending exchange rate. subscription={}, date={}, currency={}",
                    subscription.getId(), date, currency);
            return;
        }

        BigDecimal amountKrw = subscription.getAmount().multiply(rate.getRateToKrw())
                .setScale(-2, RoundingMode.HALF_UP);
        payments.save(new Payment(subscription, date, rate.getRateToKrw(), rate.getRateDate(), amountKrw));
    }
}
