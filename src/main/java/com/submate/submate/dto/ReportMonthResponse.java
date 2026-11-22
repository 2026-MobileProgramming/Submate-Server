package com.submate.submate.dto;

//연간 리포트에서 월별 차트에 사용할 DTO
//{
//  "month": 12,
//  "amountKrw": 0,
//  "future": true,
//  "categories": []
//}
import java.util.List;

public class ReportMonthResponse {

    private int month;
    private long amountKrw;
    private boolean future;
    private List<ReportCategoryResponse> categories;

    public ReportMonthResponse(
            int month,
            long amountKrw,
            boolean future,
            List<ReportCategoryResponse> categories
    ) {
        this.month = month;
        this.amountKrw = amountKrw;
        this.future = future;
        this.categories = categories;
    }

    public int getMonth() {
        return month;
    }

    public long getAmountKrw() {
        return amountKrw;
    }

    public boolean isFuture() {
        return future;
    }

    public List<ReportCategoryResponse> getCategories() {
        return categories;
    }
}