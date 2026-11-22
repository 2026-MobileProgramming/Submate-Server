package com.submate.submate.dto;

public class ReportYearResponse {

    private int year;
    private long amountKrw;

    public ReportYearResponse(int year, long amountKrw) {
        this.year = year;
        this.amountKrw = amountKrw;
    }

    public int getYear() {
        return year;
    }

    public long getAmountKrw() {
        return amountKrw;
    }
}