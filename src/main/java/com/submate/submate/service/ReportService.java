package com.submate.submate.service;

import com.submate.submate.common.PaymentDateCalculator;
import com.submate.submate.common.CategoryCode;
import com.submate.submate.dto.ReportServiceResponse;
import com.submate.submate.dto.ReportCategoryResponse;
import com.submate.submate.dto.MonthlyReportResponse;
import com.submate.submate.dto.YearlyReportResponse;
import com.submate.submate.dto.ReportMonthResponse;
import com.submate.submate.dto.AllReportResponse;
import com.submate.submate.dto.ReportYearResponse;
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
import java.util.Set;
import java.util.HashSet;
import java.util.TreeMap;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal DEFAULT_USD_RATE = new BigDecimal("1357");
    private static final BigDecimal DEFAULT_JPY_RATE = new BigDecimal("9.1");

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
        BigDecimal rateToKrw = exchangeRateRepository
                .findTopByCurrencyOrderByRateDateDesc(currency)
                .map(ExchangeRate::getRateToKrw)
                .orElse("JPY".equals(currency) ? DEFAULT_JPY_RATE : DEFAULT_USD_RATE);

        return amount
                .multiply(rateToKrw)
                .divide(HUNDRED, 0, RoundingMode.HALF_UP)
                .multiply(HUNDRED)
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

    public YearlyReportResponse getYearlyReport(Long userId, int year) {

        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);

        List<Subscription> subscriptions =
                subscriptionRepository.findByUserId(userId);

        List<Payment> payments =
                paymentRepository.findAllForReport(userId);

        List<ReportPayment> reportPayments =
                calculateReportPayments(subscriptions, payments);

        Map<YearMonth, Long> monthlyAmounts =
                calculateMonthlyAmounts(reportPayments);

        List<ReportMonthResponse> months = new ArrayList<>();
        Map<String, Long> yearlyCategoryAmounts = new HashMap<>();

        long totalAmountKrw = 0;

        // 항상 1월부터 12월까지 생성
        for (int month = 1; month <= 12; month++) {

            YearMonth targetMonth = YearMonth.of(year, month);
            boolean future = targetMonth.isAfter(currentMonth);

            if (future) {
                months.add(new ReportMonthResponse(
                        month,
                        0L,
                        true,
                        List.of()
                ));
                continue;
            }

            long amountKrw =
                    monthlyAmounts.getOrDefault(targetMonth, 0L);

            totalAmountKrw += amountKrw;

            Map<String, Long> monthlyCategories =
                    calculateMonthlyCategoryAmounts(
                            reportPayments,
                            targetMonth
                    );

            // 연간 카테고리 합계에 반영
            for (Map.Entry<String, Long> entry
                    : monthlyCategories.entrySet()) {

                yearlyCategoryAmounts.merge(
                        entry.getKey(),
                        entry.getValue(),
                        Long::sum
                );
            }

            // 월별 카테고리는 비율 없이 금액만 전달
            List<ReportCategoryResponse> monthCategories =
                    new ArrayList<>();

            monthlyCategories.entrySet().stream()
                    .sorted(
                            Map.Entry.<String, Long>comparingByValue()
                                    .reversed()
                    )
                    .forEach(entry ->
                            monthCategories.add(
                                    new ReportCategoryResponse(
                                            entry.getKey(),
                                            entry.getValue()
                                    )
                            )
                    );

            months.add(new ReportMonthResponse(
                    month,
                    amountKrw,
                    false,
                    monthCategories
            ));
        }

        // 연간 카테고리 비율
        List<ReportCategoryResponse> categories =
                calculateCategoryRatios(yearlyCategoryAmounts);

        // 월평균 계산
        int divisor = 12;

        if (year == today.getYear()) {

            // 올해 첫 결제 발생 월부터 현재 월까지
            int firstPaymentMonth = reportPayments.stream()
                    .filter(p -> p.paymentDate().getYear() == year)
                    .mapToInt(p -> p.paymentDate().getMonthValue())
                    .min()
                    .orElse(today.getMonthValue());

            divisor = today.getMonthValue() - firstPaymentMonth + 1;
        }

        long monthlyAverageKrw = Math.round(
                (double) totalAmountKrw / divisor
        );

        return new YearlyReportResponse(
                year,
                totalAmountKrw,
                monthlyAverageKrw,
                categories,
                months
        );
    }


    public AllReportResponse getAllReport(Long userId) {

        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();

        List<Subscription> subscriptions =
                subscriptionRepository.findByUserId(userId);

        List<Payment> payments =
                paymentRepository.findAllForReport(userId);

        // 오늘까지 도래한 결제 일정
        List<ReportPayment> reportPayments =
                calculateReportPayments(subscriptions, payments);

        Map<Integer, Long> yearlyAmounts = new TreeMap<>();
        Map<String, Long> categoryAmounts = new HashMap<>();
        Set<Long> paidSubscriptionIds = new HashSet<>();

        YearMonth startMonth = null;
        long totalAmountKrw = 0;

        for (ReportPayment payment : reportPayments) {

            YearMonth paymentMonth =
                    YearMonth.from(payment.paymentDate());

            if (startMonth == null
                    || paymentMonth.isBefore(startMonth)) {
                startMonth = paymentMonth;
            }

            totalAmountKrw += payment.amountKrw();

            yearlyAmounts.merge(
                    payment.paymentDate().getYear(),
                    payment.amountKrw(),
                    Long::sum
            );

            categoryAmounts.merge(
                    payment.category(),
                    payment.amountKrw(),
                    Long::sum
            );

            paidSubscriptionIds.add(payment.subscriptionId());
        }

        // 올해 남은 결제 예정 금액
        long remainingAmountKrw = 0;

        LocalDate tomorrow = today.plusDays(1);
        LocalDate yearEnd = LocalDate.of(currentYear, 12, 31);

        if (!tomorrow.isAfter(yearEnd)) {

            for (Subscription subscription : subscriptions) {

                if (!"PAID".equals(subscription.getPlanType())
                        || "CANCELED".equals(subscription.getStatus())
                        || subscription.getStartDate() == null
                        || subscription.getBillingCycle() == null
                        || subscription.getBillingDay() == null
                        || subscription.getAmount() == null) {
                    continue;
                }

                LocalDate endDate = yearEnd;

                if (subscription.getCancelDate() != null
                        && subscription.getCancelDate().minusDays(1).isBefore(endDate)) {
                    endDate = subscription.getCancelDate().minusDays(1);
                }

                if (tomorrow.isAfter(endDate)) {
                    continue;
                }

                List<LocalDate> futureDates =
                        PaymentDateCalculator.paymentDatesBetween(
                                subscription.getBillingCycle(),
                                subscription.getStartDate(),
                                subscription.getBillingDay(),
                                tomorrow,
                                endDate
                        );

                long amountKrw = convertToKrw(subscription);

                remainingAmountKrw +=
                        amountKrw * futureDates.size();
            }
        }

        long expectedAmountKrw =
                yearlyAmounts.getOrDefault(currentYear, 0L)
                        + remainingAmountKrw;

        // 올해는 결제 기록이 없어도 예상 금액을 표시
        yearlyAmounts.putIfAbsent(currentYear, 0L);

        List<ReportYearResponse> years = new ArrayList<>();

        for (Map.Entry<Integer, Long> entry : yearlyAmounts.entrySet()) {

            int year = entry.getKey();

            years.add(new ReportYearResponse(
                    year,
                    entry.getValue(),
                    year == currentYear ? expectedAmountKrw : null
            ));
        }

        return new AllReportResponse(
                startMonth == null ? null : startMonth.toString(),
                totalAmountKrw,
                paidSubscriptionIds.size(),
                calculateCategoryRatios(categoryAmounts),
                years
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

        Map<Long, Subscription> subscriptionMap = new HashMap<>();

        for (Subscription subscription : subscriptions) {
            subscriptionMap.put(subscription.getId(), subscription);
        }

        for (Payment payment : payments) {

            Subscription subscription = subscriptionMap.get(payment.getSubscription().getId());

            if (subscription == null || payment.getPaidDate().isAfter(today)) {
                continue;
            }

            results.add(new ReportPayment(
                    subscription.getId(),
                    subscription.getServiceName(),
                    CategoryCode.fromId(subscription.getCategory().getId()).name(),
                    payment.getPaidDate(),
                    payment.getAmountKrw().longValue()
            ));
        }

        for (Subscription subscription : subscriptions) {

            if (!"PAID".equals(subscription.getPlanType())
                    || "CANCELED".equals(subscription.getStatus())
                    || subscription.getStartDate() == null
                    || subscription.getBillingCycle() == null
                    || subscription.getBillingDay() == null
                    || subscription.getAmount() == null) {
                continue;
            }

            LocalDate startDate = subscription.getStartDate();
            LocalDate endDate = today;

            if (subscription.getCancelDate() != null
                    && subscription.getCancelDate().minusDays(1).isBefore(endDate)) {
                endDate = subscription.getCancelDate().minusDays(1);
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

                if (subscriptionPayments.containsKey(paymentDate)) {
                    continue;
                }

                long amountKrw = convertToKrw(subscription);

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