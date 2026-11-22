package com.submate.submate.service;

import com.submate.submate.dto.PaymentResponse;
import com.submate.submate.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class PaymentService {

    // 한 번에 조회할 수 있는 최대 기간
    private static final int MAX_YEARS = 10;

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    // from ~ to(둘 다 포함) 사이의 내 결제 내역
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPayments(Long userId, String from, String to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from, to는 필수입니다. (yyyy-MM-dd)");
        }

        LocalDate fromDate;
        LocalDate toDate;
        try {
            fromDate = LocalDate.parse(from);
            toDate = LocalDate.parse(to);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("날짜 형식은 yyyy-MM-dd 입니다.");
        }

        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("from은 to보다 늦을 수 없습니다.");
        }
        if (toDate.isAfter(fromDate.plusYears(MAX_YEARS))) {
            throw new IllegalArgumentException("조회 기간은 최대 " + MAX_YEARS + "년입니다.");
        }

        return paymentRepository.findAllForPeriod(userId, fromDate, toDate)
                .stream()
                .map(PaymentResponse::from)
                .toList();
    }
}
