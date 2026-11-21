package com.submate.submate.dto;

//프론트엔드에서 비밀번호 재설정 요청을 보낼 때 전달하는 데이터를 담는 역할입니다.
//예시
//        {
//                "email": "test@example.com",
//                "newPassword": "NewPassword123!",
//                "newPasswordConfirm": "NewPassword123!"
//                }

public class PasswordResetRequest {

    private String email;
    private String newPassword;
    private String newPasswordConfirm;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPasswordConfirm() {
        return newPasswordConfirm;
    }

    public void setNewPasswordConfirm(String newPasswordConfirm) {
        this.newPasswordConfirm = newPasswordConfirm;
    }
}