package com.submate.submate.service;

import com.submate.submate.common.ApiErrorCode;
import com.submate.submate.common.ApiException;
import com.submate.submate.common.CategoryCode;
import com.submate.submate.common.PaymentDateCalculator;
import com.submate.submate.dto.SubscriptionCreateRequest;
import com.submate.submate.dto.SubscriptionResponse;
import com.submate.submate.dto.SubscriptionUpdateRequest;
import com.submate.submate.entity.Category;
import com.submate.submate.entity.ServiceItem;
import com.submate.submate.entity.Subscription;
import com.submate.submate.entity.User;
import com.submate.submate.exception.SubscriptionNotFoundException;
import com.submate.submate.repository.CategoryRepository;
import com.submate.submate.repository.ServiceItemRepository;
import com.submate.submate.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class SubscriptionService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final List<String> STATUSES = List.of("ACTIVE", "CANCEL_SCHEDULED", "CANCELED");
    private static final List<String> CURRENCIES = List.of("KRW", "JPY", "USD");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("9999999999.99");

    private final SubscriptionRepository subscriptionRepository;
    private final CategoryRepository categoryRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final PaymentRecordingService paymentRecordingService;

    public SubscriptionService(
            SubscriptionRepository subscriptionRepository,
            CategoryRepository categoryRepository,
            ServiceItemRepository serviceItemRepository,
            PaymentRecordingService paymentRecordingService
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.categoryRepository = categoryRepository;
        this.serviceItemRepository = serviceItemRepository;
        this.paymentRecordingService = paymentRecordingService;
    }

    // 내 구독 목록 조회
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getMySubscriptions(Long userId) {

        List<Subscription> subscriptions =
                subscriptionRepository.findByUserIdOrderByIdAsc(userId);

        return subscriptions.stream()
                .map(SubscriptionResponse::new)
                .toList();
    }

    // 구독 등록
    @Transactional
    public SubscriptionResponse createSubscription(
            User user,
            SubscriptionCreateRequest request
    ) {

        // 1. 카테고리 이름을 CategoryCode로 변환
        CategoryCode categoryCode =
                CategoryCode.fromName(request.getCategory());

        // 2. DB에서 해당 카테고리 조회
        Category category = categoryRepository.findById(categoryCode.getId())
                .orElseThrow(() ->
                        new ApiException(ApiErrorCode.INVALID_INPUT, "존재하지 않는 카테고리입니다."));

        // 3. 등록 날짜
        LocalDate today = LocalDate.now(SEOUL);

        // 4. 유료 구독 / 무료 체험 구분
        boolean isTrial = isTrial(request.getCurrency(), request.getPlanType());

        String serviceName = validateServiceName(request.getServiceName());
        String status = validateStatus(request.getStatus());
        validateLength(request.getMemo(), 500, "메모");
        validateLength(request.getPackageName(), 150, "패키지명");

        LocalDate startDate = null;
        LocalDate trialEndDate = null;

        String currency = null;
        BigDecimal amount = null;
        String billingCycle = null;
        Integer billingDay = null;

        if (isTrial) {

            // 무료 체험 종료일 계산
            trialEndDate = PaymentDateCalculator.trialEndDate(
                    requireMonth(request.getPaymentMonth()),
                    requireDay(request.getPaymentDay()),
                    today
            );

        } else {

            // 유료 구독 정보
            currency = validateCurrency(request.getCurrency());
            amount = validateAmount(request.getAmount(), currency);
            billingCycle = validateBillingCycle(request.getBillingCycle());
            billingDay = requireDay(billingDayOf(request.getBillingDay(), request.getPaymentDay()));
            Integer billingMonth = "YEARLY".equals(billingCycle)
                    ? Integer.valueOf(requireMonth(request.getPaymentMonth()))
                    : null;

            // 첫 결제일 계산
            startDate = PaymentDateCalculator.firstPaymentDate(
                    billingCycle,
                    billingMonth,
                    billingDay,
                    today
            );
        }

        // 5. 구독 객체 생성
        Subscription subscription = new Subscription(
                user,
                category,
                findService(category, serviceName),
                serviceName,
                isTrial ? "TRIAL" : "PAID",
                currency,
                amount,
                billingCycle,
                billingDay,
                startDate,
                trialEndDate,
                request.getMemo(),
                request.getPackageName()
        );
        subscription.changeStatus(
                status,
                cancelDateFor(status, isTrial, billingCycle, startDate, billingDay, today)
        );

        // 6. DB 저장
        Subscription savedSubscription =
                subscriptionRepository.save(subscription);
        paymentRecordingService.recordFirstPaymentIfDue(savedSubscription, today);

        // 7. 응답 DTO 반환
        return new SubscriptionResponse(savedSubscription);
    }

    @Transactional
    public SubscriptionResponse updateSubscription(
            Long userId,
            Long subscriptionId,
            SubscriptionUpdateRequest request
    ) {
        // 1. 기존 구독 조회 (본인 소유인지 확인)
        Subscription subscription = subscriptionRepository
                .findByIdAndUserId(subscriptionId, userId)
                .orElseThrow(SubscriptionNotFoundException::new);

        // 2. 카테고리 조회
        CategoryCode categoryCode = CategoryCode.fromName(request.getCategory());

        Category category = categoryRepository.findById(categoryCode.getId())
                .orElseThrow(() -> new ApiException(ApiErrorCode.INVALID_INPUT, "존재하지 않는 카테고리입니다."));

        // 3. 유료 / 무료 체험 구분
        boolean isTrial = isTrial(request.getCurrency(), request.getPlanType());

        LocalDate today = LocalDate.now(SEOUL);

        String serviceName = validateServiceName(request.getServiceName());
        String status = validateStatus(request.getStatus());
        validateLength(request.getMemo(), 500, "메모");
        validateLength(request.getPackageName(), 150, "패키지명");

        boolean reactivated = "CANCELED".equals(subscription.getStatus())
                && !"CANCELED".equals(status);
        boolean billingChanged = false;

        LocalDate startDate = null;
        LocalDate trialEndDate = null;

        String currency = null;
        BigDecimal amount = null;
        String billingCycle = null;
        Integer billingDay = null;

        // 4. 무료 체험 데이터 처리
        if (isTrial) {
            int month = requireMonth(request.getPaymentMonth());
            int day = requireDay(request.getPaymentDay());
            LocalDate oldTrialEndDate = subscription.getTrialEndDate();

            if (!reactivated
                    && oldTrialEndDate != null
                    && oldTrialEndDate.getMonthValue() == month
                    && oldTrialEndDate.getDayOfMonth() == day) {
                trialEndDate = oldTrialEndDate;
            } else {
                trialEndDate = PaymentDateCalculator.trialEndDate(month, day, today);
            }

        } else {
            // 5. 유료 구독 데이터 처리
            currency = validateCurrency(request.getCurrency());
            amount = validateAmount(request.getAmount(), currency);
            billingCycle = validateBillingCycle(request.getBillingCycle());
            billingDay = requireDay(billingDayOf(request.getBillingDay(), request.getPaymentDay()));
            Integer billingMonth = "YEARLY".equals(billingCycle)
                    ? Integer.valueOf(requireMonth(request.getPaymentMonth()))
                    : null;

            LocalDate oldStartDate = subscription.getStartDate();
            billingChanged = !"PAID".equals(subscription.getPlanType())
                    || oldStartDate == null
                    || !billingCycle.equals(subscription.getBillingCycle())
                    || !billingDay.equals(subscription.getBillingDay())
                    || (billingMonth != null && billingMonth != oldStartDate.getMonthValue());

            if (billingChanged || reactivated) {
                startDate = PaymentDateCalculator.firstPaymentDate(
                        billingCycle,
                        billingMonth,
                        billingDay,
                        today
                );
            } else {
                startDate = oldStartDate;
            }
        }

        LocalDate cancelDate;
        if ("CANCEL_SCHEDULED".equals(status)
                && !isTrial
                && "CANCEL_SCHEDULED".equals(subscription.getStatus())
                && subscription.getCancelDate() != null
                && !billingChanged) {
            cancelDate = subscription.getCancelDate();
        } else {
            cancelDate = cancelDateFor(status, isTrial, billingCycle, startDate, billingDay, today);
        }

        // 6. 기존 구독 정보 업데이트
        subscription.updateSubscription(
                category,
                serviceName,
                isTrial ? "TRIAL" : "PAID",
                currency,
                amount,
                billingCycle,
                billingDay,
                startDate,
                trialEndDate,
                request.getMemo(),
                request.getPackageName()
        );
        subscription.changeStatus(status, cancelDate);

        // 7. 수정된 구독 반환
        return new SubscriptionResponse(subscription);
    }

    @Transactional
    public void deleteSubscription(Long userId, Long subscriptionId) {
        // 1. 구독 조회 및 소유권 확인
        Subscription subscription = subscriptionRepository
                .findByIdAndUserId(subscriptionId, userId)
                .orElseThrow(SubscriptionNotFoundException::new);

        // 2. 구독 삭제
        subscriptionRepository.delete(subscription);
    }

    private boolean isTrial(String currency, String planType) {
        return "FREE_TRIAL".equals(currency) || "TRIAL".equals(planType);
    }

    private LocalDate cancelDateFor(String status, boolean isTrial, String billingCycle,
                                    LocalDate startDate, Integer billingDay, LocalDate today) {
        if (!"CANCEL_SCHEDULED".equals(status) || isTrial) {
            return null;
        }
        return PaymentDateCalculator.nextPaymentDate(billingCycle, startDate, billingDay, today);
    }

    private ServiceItem findService(Category category, String serviceName) {
        return serviceItemRepository
                .findByCategoryIdAndName(category.getId(), serviceName)
                .orElse(null);
    }

    private String validateServiceName(String serviceName) {
        if (serviceName == null || serviceName.isBlank()) {
            throw new ApiException(ApiErrorCode.INVALID_INPUT, "서비스명은 필수입니다.");
        }
        String trimmed = serviceName.trim();
        if (trimmed.length() > 50) {
            throw new ApiException(ApiErrorCode.INVALID_INPUT, "서비스명은 50자 이하여야 합니다.");
        }
        return trimmed;
    }

    private String validateStatus(String status) {
        if (status == null || status.isBlank()) {
            return "ACTIVE";
        }
        if (!STATUSES.contains(status)) {
            throw new ApiException(ApiErrorCode.INVALID_INPUT, "구독 상태가 올바르지 않습니다.");
        }
        return status;
    }

    private String validateCurrency(String currency) {
        if (currency == null || !CURRENCIES.contains(currency)) {
            throw new ApiException(ApiErrorCode.INVALID_INPUT, "통화는 KRW, JPY, USD, FREE_TRIAL 중 하나여야 합니다.");
        }
        return currency;
    }

    private BigDecimal validateAmount(BigDecimal amount, String currency) {
        if (amount == null || amount.signum() < 0 || amount.compareTo(MAX_AMOUNT) > 0) {
            throw new ApiException(ApiErrorCode.INVALID_INPUT, "결제 금액이 올바르지 않습니다.");
        }
        int scale = Math.max(amount.stripTrailingZeros().scale(), 0);
        if (("KRW".equals(currency) || "JPY".equals(currency)) && scale > 0) {
            throw new ApiException(ApiErrorCode.INVALID_INPUT, "KRW, JPY는 정수 금액만 입력할 수 있습니다.");
        }
        if (scale > 2) {
            throw new ApiException(ApiErrorCode.INVALID_INPUT, "USD는 소수점 둘째 자리까지 입력할 수 있습니다.");
        }
        return amount;
    }

    private String validateBillingCycle(String billingCycle) {
        if (!"MONTHLY".equals(billingCycle) && !"YEARLY".equals(billingCycle)) {
            throw new ApiException(ApiErrorCode.INVALID_INPUT, "결제 주기는 MONTHLY, YEARLY 중 하나여야 합니다.");
        }
        return billingCycle;
    }

    private Integer billingDayOf(Integer billingDay, Integer paymentDay) {
        return billingDay != null ? billingDay : paymentDay;
    }

    private int requireDay(Integer day) {
        if (day == null || day < 1 || day > 31) {
            throw new ApiException(ApiErrorCode.INVALID_INPUT, "일은 1~31 사이여야 합니다.");
        }
        return day;
    }

    private int requireMonth(Integer month) {
        if (month == null || month < 1 || month > 12) {
            throw new ApiException(ApiErrorCode.INVALID_INPUT, "월은 1~12 사이여야 합니다.");
        }
        return month;
    }

    private void validateLength(String value, int max, String fieldName) {
        if (value != null && value.length() > max) {
            throw new ApiException(ApiErrorCode.INVALID_INPUT, fieldName + "은(는) " + max + "자 이하여야 합니다.");
        }
    }
}
