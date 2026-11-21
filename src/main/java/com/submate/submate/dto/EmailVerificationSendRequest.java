package com.submate.submate.dto;

//Postman이나 Android 앱에서 JSON으로 이메일과 인증 목적을 보내면
//Spring Boot가 자동으로 이 객체에 데이터를 담아줌.

public class EmailVerificationSendRequest {

    private String email;
    private String purpose;

    public EmailVerificationSendRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}
