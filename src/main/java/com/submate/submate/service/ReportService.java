package com.submate.submate.service;

import com.submate.submate.common.PaymentDateCalculator;
import com.submate.submate.common.CategoryCode;
import com.submate.submate.dto.ReportServiceResponse;
import com.submate.submate.dto.ReportCategoryResponse;
import com.submate.submate.dto.MonthlyReportResponse;
import com.submate.submate.repository.PaymentRepository;
import com.submate.submate.repository.ExchangeRateRepository;
import com.submate.submate.repository.SubscriptionRepository;
import com.submate.submate.entity.Payment;
import com.submate.submate.entity.Subscription;
import com.submate.submate.entity.ExchangeRate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final PaymentRepository paymentRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final SubscriptionRepository subscriptionRepository;


    public ReportService(
            PaymentRepository paymentRepository,
            ExchangeRateRepository exchangeRateRepository,
            SubscriptionRepository subscriptionRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.exchangeRateRepository = exchangeRateRepository;
        this.subscriptionRepository = subscriptionRepository;
    }



    private long convertToKrw(Subscription subscription) {

        BigDecimal amount = subscription.getAmount();
        String currency = subscription.getCurrency();

        // 원화 구독은 그대로 반환
        if ("KRW".equals(currency)) {
            return amount.setScale(0, RoundingMode.HALF_UP).longValue();
        }

        // USD, JPY는 저장된 최신 환율 사용
        ExchangeRate exchangeRate = exchangeRateRepository
                .findTopByCurrencyOrderByRateDateDesc(currency)
                .orElseThrow(() -> new IllegalStateException(
                        "저장된 환율이 없습니다: " + currency
                ));

        return amount
                .multiply(exchangeRate.getRateToKrw())
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
    }

    public MonthlyReportResponse getMonthlyReport(
            Long userId,
            int year,
            int month
    ) {
        YearMonth targetMonth = YearMonth.of(year, month);
        YearMonth previousMonth = targetMonth.minusMonths(1);

        // 현재 사용자의 구독 및 결제 기록 조회
        List<Subscription> subscriptions =
                subscriptionRepository.findByUserId(userId);

        List<Payment> payments =
                paymentRepository.findAllForReport(userId);

        // 공통 결제 데이터 생성
        List<ReportPayment> reportPayments =
                calculateReportPayments(subscriptions, payments);

        // 월별 총액 계산
        Map<YearMonth, Long> monthlyAmounts =
                calculateMonthlyAmounts(reportPayments);

        long totalAmountKrw =
                monthlyAmounts.getOrDefault(targetMonth, 0L);

        // 전월 대비 차액
        Long previousMonthDiffKrw = null;

        if (monthlyAmounts.containsKey(previousMonth)) {
            long previousAmount =
                    monthlyAmounts.get(previousMonth);

            previousMonthDiffKrw =
                    totalAmountKrw - previousAmount;
        }

        // 서비스별 지출
        List<ReportServiceResponse> services =
                calculateMonthlyServices(reportPayments, targetMonth);

        // 카테고리별 지출 및 비율
        Map<String, Long> categoryAmounts =
                calculateMonthlyCategoryAmounts(
                        reportPayments,
                        targetMonth
                );

        List<ReportCategoryResponse> categories =
                calculateCategoryRatios(categoryAmounts);

        return new MonthlyReportResponse(
                year,
                month,
                totalAmountKrw,
                previousMonthDiffKrw,
                services,
                categories
        );
    }


    private List<ReportPayment> calculateReportPayments(
            List<Subscription> subscriptions,
            List<Payment> payments
    ) {
        List<ReportPayment> results = new java.util.ArrayList<>();
        LocalDate today = LocalDate.now();

        // 구독 ID + 결제일로 과거 결제 기록 조회
        Map<Long, Map<LocalDate, Payment>> paymentMap = new HashMap<>();

        for (Payment payment : payments) {
            paymentMap
                    .computeIfAbsent(
                            payment.getSubscription().getId(),
                            id -> new HashMap<>()
                    )
                    .put(payment.getPaidDate(), payment);
        }

        for (Subscription subscription : subscriptions) {

            if (!"PAID".equals(subscription.getPlanType())
                    || subscription.getStartDate() == null
                    || subscription.getBillingCycle() == null
                    || subscription.getBillingDay() == null
                    || subscription.getAmount() == null) {
                continue;
            }

            LocalDate startDate = subscription.getStartDate();
            LocalDate endDate = today;

            if (subscription.getCancelDate() != null
                    && subscription.getCancelDate().isBefore(endDate)) {
                endDate = subscription.getCancelDate();
            }

            if (startDate.isAfter(endDate)) {
                continue;
            }

            List<LocalDate> paymentDates =
                    PaymentDateCalculator.paymentDatesBetween(
                            subscription.getBillingCycle(),
                            startDate,
                            subscription.getBillingDay(),
                            startDate,
                            endDate
                    );

            Map<LocalDate, Payment> subscriptionPayments =
                    paymentMap.getOrDefault(subscription.getId(), Map.of());

            for (LocalDate paymentDate : paymentDates) {

                Payment payment = subscriptionPayments.get(paymentDate);

                long amountKrw = payment != null
                        ? payment.getAmountKrw().longValue()
                        : convertToKrw(subscription);

                String category = CategoryCode
                        .fromId(subscription.getCategory().getId())
                        .name();

                results.add(new ReportPayment(
                        subscription.getId(),
                        subscription.getServiceName(),
                        category,
                        paymentDate,
                        amountKrw
                ));
            }
        }

        return results;
    }

    // 월별 결제 금액 집계

    private Map<YearMonth, Long> calculateMonthlyAmounts(
            List<ReportPayment> reportPayments
    ) {
        Map<YearMonth, Long> monthlyAmounts = new HashMap<>();

        for (ReportPayment payment : reportPayments) {

            YearMonth month = YearMonth.from(payment.paymentDate());

            monthlyAmounts.merge(
                    month,
                    payment.amountKrw(),
                    Long::sum
            );
        }

        return monthlyAmounts;
    }


    private List<ReportServiceResponse> calculateMonthlyServices(
            List<ReportPayment> reportPayments,
            YearMonth targetMonth
    ) {
        Map<Long, Long> serviceAmounts = new HashMap<>();
        Map<Long, ReportPayment> serviceInfo = new HashMap<>();

        for (ReportPayment payment : reportPayments) {

            // 조회한 월의 결제 건만 사용
            if (!YearMonth.from(payment.paymentDate()).equals(targetMonth)) {
                continue;
            }

            Long subscriptionId = payment.subscriptionId();

            // 같은 구독의 결제 금액 합산
            serviceAmounts.merge(
                    subscriptionId,
                    payment.amountKrw(),
                    Long::sum
            );

            serviceInfo.putIfAbsent(subscriptionId, payment);
        }

        List<ReportServiceResponse> services = new ArrayList<>();

        for (Map.Entry<Long, Long> entry : serviceAmounts.entrySet()) {

            Long subscriptionId = entry.getKey();
            ReportPayment info = serviceInfo.get(subscriptionId);

            services.add(new ReportServiceResponse(
                    subscriptionId,
                    info.serviceName(),
                    info.category(),
                    entry.getValue()
            ));
        }

        // 금액 내림차순 정렬
        services.sort(
                (a, b) -> Long.compare(
                        b.getAmountKrw(),
                        a.getAmountKrw()
                )
        );

        return services;
    }


    private Map<String, Long> calculateMonthlyCategoryAmounts(
            List<ReportPayment> reportPayments,
            YearMonth targetMonth
    ) {
        Map<String, Long> categoryAmounts = new HashMap<>();

        // 미래 월은 집계하지 않음
        if (targetMonth.isAfter(YearMonth.now())) {
            return categoryAmounts;
        }

        for (ReportPayment payment : reportPayments) {

            YearMonth paymentMonth =
                    YearMonth.from(payment.paymentDate());

            // 해당 월의 결제 건만 집계
            if (!paymentMonth.equals(targetMonth)) {
                continue;
            }

            categoryAmounts.merge(
                    payment.category(),
                    payment.amountKrw(),
                    Long::sum
            );
        }

        return categoryAmounts;
    }

    private List<ReportCategoryResponse> calculateCategoryRatios(
            Map<String, Long> categoryAmounts
    ) {
        List<ReportCategoryResponse> result = new ArrayList<>();

        long total = categoryAmounts.values().stream()
                .mapToLong(Long::longValue)
                .sum();

        if (total <= 0) {
            return result;
        }

        // 금액 내림차순, 동률이면 카테고리 이름순
        List<Map.Entry<String, Long>> sorted =
                new ArrayList<>(categoryAmounts.entrySet());

        sorted.sort(
                Map.Entry.<String, Long>comparingByValue()
                        .reversed()
                        .thenComparing(Map.Entry.comparingByKey())
        );

        Map<String, Integer> ratios = new HashMap<>();
        Map<String, Long> remainders = new HashMap<>();

        int assigned = 0;

        for (Map.Entry<String, Long> entry : sorted) {
            // 정수 나눗셈으로 소수점 이하를 일단 버림
            long scaled = entry.getValue() * 100;
            int ratio = (int) (scaled / total);

            ratios.put(entry.getKey(), ratio);
            remainders.put(entry.getKey(), scaled % total);
            assigned += ratio;
        }

        // 남은 퍼센트는 소수점 나머지가 큰 순서대로 배분
        List<String> remainderOrder = new ArrayList<>(ratios.keySet());

        remainderOrder.sort(
                (a, b) -> {
                    int compare = Long.compare(
                            remainders.get(b),
                            remainders.get(a)
                    );

                    return compare != 0 ? compare : a.compareTo(b);
                }
        );

        for (int i = 0; i < 100 - assigned; i++) {
            String category = remainderOrder.get(i);
            ratios.put(category, ratios.get(category) + 1);
        }

        for (Map.Entry<String, Long> entry : sorted) {
            result.add(new ReportCategoryResponse(
                    entry.getKey(),
                    entry.getValue(),
                    ratios.get(entry.getKey())
            ));
        }

        return result;
    }


    private record ReportPayment(
            Long subscriptionId,
            String serviceName,
            String category,
            LocalDate paymentDate,
            long amountKrw
    ) {
    }


}