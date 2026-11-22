package com.submate.submate.service;

import com.submate.submate.common.CategoryCode;
import com.submate.submate.common.PaymentDateCalculator;
import com.submate.submate.dto.SubscriptionCreateRequest;
import com.submate.submate.dto.SubscriptionResponse;
import com.submate.submate.dto.SubscriptionUpdateRequest;
import com.submate.submate.entity.Category;
import com.submate.submate.entity.Subscription;
import com.submate.submate.entity.User;
import com.submate.submate.repository.CategoryRepository;
import com.submate.submate.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final CategoryRepository categoryRepository;

    public SubscriptionService(
            SubscriptionRepository subscriptionRepository,
            CategoryRepository categoryRepository
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.categoryRepository = categoryRepository;
    }

    // 내 구독 목록 조회
    public List<SubscriptionResponse> getMySubscriptions(Long userId) {

        List<Subscription> subscriptions =
                subscriptionRepository.findByUserId(userId);

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
                        new IllegalArgumentException("존재하지 않는 카테고리입니다."));

        // 3. 등록 날짜
        LocalDate today = LocalDate.now();

        // 4. 유료 구독 / 무료 체험 구분
        boolean isTrial = "TRIAL".equals(request.getPlanType());

        LocalDate startDate = null;
        LocalDate trialEndDate = null;

        String currency = null;
        BigDecimal amount = null;
        String billingCycle = null;
        Integer billingDay = null;

        if (isTrial) {

            // 무료 체험 종료일 계산
            if (request.getPaymentMonth() == null
                    || request.getPaymentDay() == null) {
                throw new IllegalArgumentException(
                        "무료 체험 종료 월과 일이 필요합니다.");
            }

            trialEndDate = PaymentDateCalculator.trialEndDate(
                    request.getPaymentMonth(),
                    request.getPaymentDay(),
                    today
            );

            amount = null;

        } else {

            // 유료 구독 정보
            currency = request.getCurrency();
            amount = request.getAmount();
            billingCycle = request.getBillingCycle();
            billingDay = request.getBillingDay();

            if (billingCycle == null || billingDay == null) {
                throw new IllegalArgumentException(
                        "결제 주기와 결제일이 필요합니다.");
            }

            // 첫 결제일 계산
            startDate = PaymentDateCalculator.firstPaymentDate(
                    billingCycle,
                    request.getPaymentMonth(),
                    billingDay,
                    today
            );
        }

        // 5. 구독 객체 생성
        Subscription subscription = new Subscription(
                user,
                category,
                null, // 직접 입력 서비스
                request.getServiceName(),
                request.getPlanType(),
                currency,
                amount,
                billingCycle,
                billingDay,
                startDate,
                trialEndDate,
                request.getMemo(),
                request.getPackageName()
        );

        // 6. DB 저장
        Subscription savedSubscription =
                subscriptionRepository.save(subscription);

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
                .orElseThrow(() -> new IllegalArgumentException("구독을 찾을 수 없습니다."));

        // 2. 카테고리 조회
        CategoryCode categoryCode = CategoryCode.fromName(request.getCategory());

        Category category = categoryRepository.findById(categoryCode.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));

        // 3. 유료 / 무료 체험 구분
        boolean isTrial = "TRIAL".equals(request.getPlanType());

        LocalDate today = LocalDate.now();

        LocalDate startDate = null;
        LocalDate trialEndDate = null;

        String currency = null;
        BigDecimal amount = null;
        String billingCycle = null;
        Integer billingDay = null;

        // 4. 무료 체험 데이터 처리
        if (isTrial) {
            if (request.getPaymentMonth() == null || request.getPaymentDay() == null) {
                throw new IllegalArgumentException("무료 체험 종료 월과 일이 필요합니다.");
            }

            trialEndDate = PaymentDateCalculator.trialEndDate(
                    request.getPaymentMonth(),
                    request.getPaymentDay(),
                    today
            );

        } else {
            // 5. 유료 구독 데이터 처리
            currency = request.getCurrency();
            amount = request.getAmount();
            billingCycle = request.getBillingCycle();
            billingDay = request.getBillingDay();

            if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("결제 금액이 올바르지 않습니다.");
            }

            if (currency == null || billingCycle == null || billingDay == null) {
                throw new IllegalArgumentException("통화, 결제 주기, 결제일이 필요합니다.");
            }

            startDate = PaymentDateCalculator.firstPaymentDate(
                    billingCycle,
                    request.getPaymentMonth(),
                    billingDay,
                    today
            );
        }

        // 6. 기존 구독 정보 업데이트
        subscription.updateSubscription(
                category,
                request.getServiceName(),
                request.getPlanType(),
                currency,
                amount,
                billingCycle,
                billingDay,
                startDate,
                trialEndDate,
                request.getMemo(),
                request.getPackageName()
        );

        // 7. 수정된 구독 반환
        return new SubscriptionResponse(subscription);
    }
    @Transactional
    public void deleteSubscription(Long userId, Long subscriptionId) {
        // 1. 구독 조회 및 소유권 확인
        Subscription subscription = subscriptionRepository
                .findByIdAndUserId(subscriptionId, userId)
                .orElseThrow(() -> new IllegalArgumentException("구독을 찾을 수 없습니다."));

        // 2. 구독 삭제
        subscriptionRepository.delete(subscription);
    }
}