package com.submate.submate;

import com.submate.submate.dto.SubscriptionCreateRequest;
import com.submate.submate.entity.Category;
import com.submate.submate.entity.ExchangeRate;
import com.submate.submate.entity.Payment;
import com.submate.submate.entity.Subscription;
import com.submate.submate.entity.User;
import com.submate.submate.repository.CategoryRepository;
import com.submate.submate.repository.ExchangeRateRepository;
import com.submate.submate.repository.PaymentRepository;
import com.submate.submate.repository.ServiceItemRepository;
import com.submate.submate.repository.SubscriptionRepository;
import com.submate.submate.service.PaymentRecordingService;
import com.submate.submate.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentRecordingServiceTests {

    private final SubscriptionRepository subscriptions = mock(SubscriptionRepository.class);
    private final PaymentRepository payments = mock(PaymentRepository.class);
    private final ExchangeRateRepository rates = mock(ExchangeRateRepository.class);
    private final TransactionTemplate transaction = mock(TransactionTemplate.class);
    private final Subscription subscription = mock(Subscription.class);
    private final LocalDate februaryEnd = LocalDate.of(2027, 2, 28);

    @Test
    void registrationImmediatelyRecordsPaymentDueToday() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        SubscriptionCreateRequest request = paidRequest(today, "KRW", "ACTIVE");

        registrationService().createSubscription(mock(User.class), request);

        ArgumentCaptor<Payment> saved = ArgumentCaptor.forClass(Payment.class);
        verify(payments).save(saved.capture());
        assertEquals(today, saved.getValue().getPaidDate());
        assertEquals(new BigDecimal("11990"), saved.getValue().getAmountKrw());
    }

    @Test
    void registrationSkipsFuturePaymentsAndPaymentsOnCancellationDate() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        SubscriptionService service = registrationService();

        service.createSubscription(mock(User.class), paidRequest(today.plusDays(1), "KRW", "ACTIVE"));
        service.createSubscription(mock(User.class), paidRequest(today, "KRW", "CANCEL_SCHEDULED"));

        verify(payments, never()).save(any());
    }

    @Test
    void registrationSucceedsWhenPaymentRateIsUnavailable() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        when(rates.findTopByCurrencyAndRateDateLessThanEqualOrderByRateDateDesc("USD", today))
                .thenReturn(Optional.empty());

        registrationService().createSubscription(mock(User.class), paidRequest(today, "USD", "ACTIVE"));

        verify(subscriptions).save(any(Subscription.class));
        verify(rates).findTopByCurrencyAndRateDateLessThanEqualOrderByRateDateDesc("USD", today);
        verify(payments, never()).save(any());
    }

    @Test
    void recordsFromFirstPaymentDateButExcludesCancellationDate() {
        LocalDate cancelDate = LocalDate.of(2027, 3, 31);
        LocalDate today = cancelDate.plusDays(1);
        PaymentRecordingService service = prepare(today, "CANCELED", "KRW");
        when(subscription.getCancelDate()).thenReturn(cancelDate);

        service.recordThrough(today);

        ArgumentCaptor<Payment> saved = ArgumentCaptor.forClass(Payment.class);
        verify(payments).save(saved.capture());
        assertEquals(februaryEnd, saved.getValue().getPaidDate());
        assertEquals(new BigDecimal("11990"), saved.getValue().getAmountKrw());
    }

    @Test
    void retriesMissingHistoricalRateAndSkipsAlreadyRecordedPayment() {
        PaymentRecordingService service = prepare(februaryEnd, "ACTIVE", "USD");
        LocalDate rateDate = februaryEnd.minusDays(2);
        when(subscription.getAmount()).thenReturn(new BigDecimal("20.00"));
        when(rates.findTopByCurrencyAndRateDateLessThanEqualOrderByRateDateDesc("USD", februaryEnd))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new ExchangeRate(rateDate, "USD", new BigDecimal("1385.4"))));
        when(payments.findRecordedDates(1L, februaryEnd, februaryEnd))
                .thenReturn(List.of()).thenReturn(List.of()).thenReturn(List.of(februaryEnd));

        service.recordThrough(februaryEnd);
        verify(payments, never()).save(any());
        service.recordThrough(februaryEnd);
        service.recordThrough(februaryEnd);

        ArgumentCaptor<Payment> saved = ArgumentCaptor.forClass(Payment.class);
        verify(payments).save(saved.capture());
        assertEquals(rateDate, saved.getValue().getRateDate());
        assertEquals(27700, saved.getValue().getAmountKrw().intValueExact());
    }

    private PaymentRecordingService prepare(LocalDate today, String status, String currency) {
        when(subscriptions.findPaymentCandidateIds(today)).thenReturn(List.of(1L));
        when(subscriptions.findForPaymentRecording(1L)).thenReturn(Optional.of(subscription));
        when(subscription.getId()).thenReturn(1L);
        when(subscription.getPlanType()).thenReturn("PAID");
        when(subscription.getStatus()).thenReturn(status);
        when(subscription.getStartDate()).thenReturn(februaryEnd);
        when(subscription.getBillingCycle()).thenReturn("MONTHLY");
        when(subscription.getBillingDay()).thenReturn(31);
        when(subscription.getCurrency()).thenReturn(currency);
        when(subscription.getAmount()).thenReturn(new BigDecimal("11990"));
        doAnswer(invocation -> {
            Consumer<TransactionStatus> work = invocation.getArgument(0);
            work.accept(mock(TransactionStatus.class));
            return null;
        }).when(transaction).executeWithoutResult(any());
        return new PaymentRecordingService(subscriptions, payments, rates, transaction);
    }

    private SubscriptionService registrationService() {
        CategoryRepository categories = mock(CategoryRepository.class);
        Category category = mock(Category.class);
        when(category.getId()).thenReturn(1);
        when(categories.findById(1)).thenReturn(Optional.of(category));
        when(subscriptions.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));
        return new SubscriptionService(subscriptions, categories, mock(ServiceItemRepository.class),
                new PaymentRecordingService(subscriptions, payments, rates, transaction));
    }

    private SubscriptionCreateRequest paidRequest(LocalDate billingDate, String currency, String status) {
        SubscriptionCreateRequest request = new SubscriptionCreateRequest();
        request.setCategory("OTT");
        request.setServiceName("Test subscription");
        request.setPlanType("PAID");
        request.setBillingCycle("YEARLY");
        request.setPaymentMonth(billingDate.getMonthValue());
        request.setBillingDay(billingDate.getDayOfMonth());
        request.setCurrency(currency);
        request.setAmount(new BigDecimal("11990"));
        request.setStatus(status);
        return request;
    }
}
