package com.submate.submate.dto;

//월별 리포트의 전체 응답을 담는 DTO
//year, month: 조회한 연도와 월
//totalAmountKrw: 해당 월 총 지출
//previousMonthDiffKrw: 전월 대비 차액 (null 가능)
//services: 서비스별 지출 목록
//categories: 카테고리별 지출 및 비율

import java.util.List;

public class MonthlyReportResponse {

    private int year;
    private int month;
    private long totalAmountKrw;
    private Long previousMonthDiffKrw;
    private List<ReportServiceResponse> services;
    private List<ReportCategoryResponse> categories;

    public MonthlyReportResponse(
            int year,
            int month,
            long totalAmountKrw,
            Long previousMonthDiffKrw,
            List<ReportServiceResponse> services,
            List<ReportCategoryResponse> categories
    ) {
        this.year = year;
        this.month = month;
        this.totalAmountKrw = totalAmountKrw;
        this.previousMonthDiffKrw = previousMonthDiffKrw;
        this.services = services;
        this.categories = categories;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public long getTotalAmountKrw() {
        return totalAmountKrw;
    }

    public Long getPreviousMonthDiffKrw() {
        return previousMonthDiffKrw;
    }

    public List<ReportServiceResponse> getServices() {
        return services;
    }

    public List<ReportCategoryResponse> getCategories() {
        return categories;
    }
}