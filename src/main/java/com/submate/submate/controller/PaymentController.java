package com.submate.submate.controller;

import com.submate.submate.dto.PaymentResponse;
import com.submate.submate.entity.User;
import com.submate.submate.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // 결제 내역 조회 (캘린더 과거 금액)
    // GET /api/payments?from=2026-11-01&to=2026-11-30
    @GetMapping
    public ResponseEntity<?> getPayments(
            Authentication authentication,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        User user = (User) authentication.getPrincipal();

        try {
            List<PaymentResponse> payments = paymentService.getPayments(user.getId(), from, to);
            return ResponseEntity.ok(payments);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("code", "INVALID_INPUT", "message", e.getMessage())
            );
        }
    }
}
