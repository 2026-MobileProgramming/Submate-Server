
package com.submate.submate.controller;

import com.submate.submate.dto.MonthlyReportResponse;
import com.submate.submate.service.ReportService;
import com.submate.submate.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(
            @RequestParam int year,
            @RequestParam int month,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();

        MonthlyReportResponse response =
                reportService.getMonthlyReport(userId, year, month);

        return ResponseEntity.ok(response);
    }
}
