package com.submate.submate.dto;

//월별 리포트의 서비스별 지출 목록에 사용할 DTO
//응답 예시
//{
//  "subscriptionId": 12,
//  "serviceName": "Netflix",
//  "category": "OTT",
//  "amountKrw": 17000
//}
public class ReportServiceResponse {

    private Long subscriptionId;
    private String serviceName;
    private String category;
    private long amountKrw;

    public ReportServiceResponse(
            Long subscriptionId,
            String serviceName,
            String category,
            long amountKrw
    ) {
        this.subscriptionId = subscriptionId;
        this.serviceName = serviceName;
        this.category = category;
        this.amountKrw = amountKrw;
    }

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getCategory() {
        return category;
    }

    public long getAmountKrw() {
        return amountKrw;
    }
}