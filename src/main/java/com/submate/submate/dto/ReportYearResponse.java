
package com.submate.submate.dto;

public class ReportYearResponse {

    private int year;
    private long amountKrw;
    private Long expectedAmountKrw;

    public ReportYearResponse(
            int year,
            long amountKrw,
            Long expectedAmountKrw
    ) {
        this.year = year;
        this.amountKrw = amountKrw;
        this.expectedAmountKrw = expectedAmountKrw;
    }

    // 기존 생성자와의 호환성 유지
    public ReportYearResponse(int year, long amountKrw) {
        this(year, amountKrw, null);
    }

    public int getYear() {
        return year;
    }

    public long getAmountKrw() {
        return amountKrw;
    }

    public Long getExpectedAmountKrw() {
        return expectedAmountKrw;
    }
}
