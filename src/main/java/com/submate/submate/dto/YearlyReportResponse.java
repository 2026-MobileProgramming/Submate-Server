package com.submate.submate.dto;

//연간 리포트 전체 응답 DTO

import java.util.List;

public class YearlyReportResponse {

    private int year;
    private long totalAmountKrw;
    private long monthlyAverageKrw;
    private List<ReportCategoryResponse> categories;
    private List<ReportMonthResponse> months;

    public YearlyReportResponse(
            int year,
            long totalAmountKrw,
            long monthlyAverageKrw,
            List<ReportCategoryResponse> categories,
            List<ReportMonthResponse> months
    ) {
        this.year = year;
        this.totalAmountKrw = totalAmountKrw;
        this.monthlyAverageKrw = monthlyAverageKrw;
        this.categories = categories;
        this.months = months;
    }

    public int getYear() {
        return year;
    }

    public long getTotalAmountKrw() {
        return totalAmountKrw;
    }

    public long getMonthlyAverageKrw() {
        return monthlyAverageKrw;
    }

    public List<ReportCategoryResponse> getCategories() {
        return categories;
    }

    public List<ReportMonthResponse> getMonths() {
        return months;
    }
}