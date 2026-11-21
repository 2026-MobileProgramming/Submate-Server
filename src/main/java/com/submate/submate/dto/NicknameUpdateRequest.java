package com.submate.submate.dto;

//프론트엔드에서 변경할 닉네임을 전달받는 DTO
//닉네임 규칙
//  한글, 영어, 숫자 허용
//  1~8자
//  특수문자 불가


public class NicknameUpdateRequest {

    private String nickname;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}