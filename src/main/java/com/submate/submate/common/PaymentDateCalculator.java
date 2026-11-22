package com.submate.submate.common;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

// 결제일 계산 (구독 API, 스케줄러, 캘린더 공통)
// - 그 달에 없는 날짜(31일 등)는 말일로 맞춤
// - 연간 2/29는 평년에 2/28
public final class PaymentDateCalculator {

    public static final String MONTHLY = "MONTHLY";
    public static final String YEARLY = "YEARLY";

    private PaymentDateCalculator() {
    }

    // year년 month월의 day일 (없는 날짜면 그 달 말일)
    public static LocalDate dateOf(int year, int month, int day) {
        validateMonth(month);
        validateDay(day);
        YearMonth yearMonth = YearMonth.of(year, month);
        return yearMonth.atDay(Math.min(day, yearMonth.lengthOfMonth()));
    }

    // 첫 결제일: from(등록일, 당일 포함) 이후 처음 오는 결제일
    // MONTHLY는 billingMonth를 쓰지 않음
    public static LocalDate firstPaymentDate(String billingCycle, Integer billingMonth, int billingDay, LocalDate from) {
        if (MONTHLY.equals(billingCycle)) {
            LocalDate candidate = dateOf(from.getYear(), from.getMonthValue(), billingDay);
            if (candidate.isBefore(from)) {
                LocalDate nextMonth = from.plusMonths(1);
                candidate = dateOf(nextMonth.getYear(), nextMonth.getMonthValue(), billingDay);
            }
            return candidate;
        }
        if (YEARLY.equals(billingCycle)) {
            if (billingMonth == null) {
                throw new IllegalArgumentException("연간 결제는 결제 월이 필요합니다.");
            }
            LocalDate candidate = dateOf(from.getYear(), billingMonth, billingDay);
            if (candidate.isBefore(from)) {
                candidate = dateOf(from.getYear() + 1, billingMonth, billingDay);
            }
            return candidate;
        }
        throw new IllegalArgumentException("알 수 없는 결제 주기: " + billingCycle);
    }

    // 다음 결제일: startDate부터 반복되는 결제일 중 today 이후(당일 포함) 가장 가까운 날
    // 연간 결제의 월은 startDate의 월을 사용
    public static LocalDate nextPaymentDate(String billingCycle, LocalDate startDate, int billingDay, LocalDate today) {
        if (!today.isAfter(startDate)) {
            return startDate;
        }
        return firstPaymentDate(billingCycle, startDate.getMonthValue(), billingDay, today);
    }

    // from ~ to(둘 다 포함) 사이의 모든 결제일 (startDate 이전은 제외)
    public static List<LocalDate> paymentDatesBetween(String billingCycle, LocalDate startDate, int billingDay,
                                                      LocalDate from, LocalDate to) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate begin = from.isBefore(startDate) ? startDate : from;
        if (begin.isAfter(to)) {
            return dates;
        }
        LocalDate date = nextPaymentDate(billingCycle, startDate, billingDay, begin);
        while (!date.isAfter(to)) {
            dates.add(date);
            date = nextPaymentDate(billingCycle, startDate, billingDay, date.plusDays(1));
        }
        return dates;
    }

    // 무료체험 종료일: 앱은 월·일만 보내므로 today(당일 포함) 이후 처음 오는 그 월·일로 정함
    public static LocalDate trialEndDate(int month, int day, LocalDate today) {
        LocalDate candidate = dateOf(today.getYear(), month, day);
        if (candidate.isBefore(today)) {
            candidate = dateOf(today.getYear() + 1, month, day);
        }
        return candidate;
    }

    private static void validateMonth(int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("월은 1~12 사이여야 합니다: " + month);
        }
    }

    private static void validateDay(int day) {
        if (day < 1 || day > 31) {
            throw new IllegalArgumentException("일은 1~31 사이여야 합니다: " + day);
        }
    }
}
