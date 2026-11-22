package com.submate.submate.dto;

import java.util.List;

public class AllReportResponse {

    private String startMonth;
    private long totalAmountKrw;
    private int subscriptionCount;
    private List<ReportCategoryResponse> categories;
    private List<ReportYearResponse> years;

    public AllReportResponse(
            String startMonth,
            long totalAmountKrw,
            int subscriptionCount,
            List<ReportCategoryResponse> categories,
            List<ReportYearResponse> years
    ) {
        this.startMonth = startMonth;
        this.totalAmountKrw = totalAmountKrw;
        this.subscriptionCount = subscriptionCount;
        this.categories = categories;
        this.years = years;
    }

    public String getStartMonth() {
        return startMonth;
    }

    public long getTotalAmountKrw() {
        return totalAmountKrw;
    }

    public int getSubscriptionCount() {
        return subscriptionCount;
    }

    public List<ReportCategoryResponse> getCategories() {
        return categories;
    }

    public List<ReportYearResponse> getYears() {
        return years;
    }
}