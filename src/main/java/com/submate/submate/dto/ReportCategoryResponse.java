package com.submate.submate.dto;

//월별, 연간, 전체 리포트에서 카테고리별 지출을 표현 DTO


public class ReportCategoryResponse {

    private String category;
    private long amountKrw;
    private Integer ratio;

    public ReportCategoryResponse(String category, long amountKrw, Integer ratio) {
        this.category = category;
        this.amountKrw = amountKrw;
        this.ratio = ratio;
    }

    public ReportCategoryResponse(String category, long amountKrw) {
        this.category = category;
        this.amountKrw = amountKrw;
        this.ratio = null;
    }

    public String getCategory() {
        return category;
    }

    public long getAmountKrw() {
        return amountKrw;
    }

    public Integer getRatio() {
        return ratio;
    }
}