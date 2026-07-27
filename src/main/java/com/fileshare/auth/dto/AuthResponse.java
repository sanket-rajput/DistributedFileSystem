package com.fileshare.auth.dto;

import com.fileshare.user.dto.UserResponseDto;

public class AuthResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private UserResponseDto user;

    public AuthResponse() {
    }

    public AuthResponse(String accessToken, String tokenType, UserResponseDto user) {
        this.accessToken = accessToken;
        this.tokenType = tokenType != null ? tokenType : "Bearer";
        this.user = user;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public UserResponseDto getUser() { return user; }
    public void setUser(UserResponseDto user) { this.user = user; }

    public static AuthResponseBuilder builder() {
        return new AuthResponseBuilder();
    }

    public static class AuthResponseBuilder {
        private String accessToken;
        private String tokenType = "Bearer";
        private UserResponseDto user;

        public AuthResponseBuilder accessToken(String accessToken) { this.accessToken = accessToken; return this; }
        public AuthResponseBuilder tokenType(String tokenType) { this.tokenType = tokenType; return this; }
        public AuthResponseBuilder user(UserResponseDto user) { this.user = user; return this; }

        public AuthResponse build() {
            return new AuthResponse(accessToken, tokenType, user);
        }
    }
}
