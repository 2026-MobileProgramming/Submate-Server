package com.submate.submate.dto;

//사용자 정보 조회에 필요한 정보만 담는 DTO(민감한 정보 반환을 방지하기 위해)
//예시 데이터
//{
//    "id": 1,
//    "email": "user@example.com",
//    "nickname": "섭메이트",
//    "createdAt": "2026-11-16T20:49:00"
//}

import java.time.LocalDateTime;

public class UserResponse {

    private Long id;
    private String email;
    private String nickname;
    private LocalDateTime createdAt;

    public UserResponse(
            Long id,
            String email,
            String nickname,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}