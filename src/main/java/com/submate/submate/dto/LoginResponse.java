package com.submate.submate.dto;

public class LoginResponse {

    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private UserInfo user;

    public LoginResponse(
            String accessToken,
            String tokenType,
            long expiresIn,
            UserInfo user
    ) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public UserInfo getUser() {
        return user;
    }

    public static class UserInfo {

        private Long id;
        private String email;
        private String nickname;

        public UserInfo(Long id, String email, String nickname) {
            this.id = id;
            this.email = email;
            this.nickname = nickname;
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
    }
}