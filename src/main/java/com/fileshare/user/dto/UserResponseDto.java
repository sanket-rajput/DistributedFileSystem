package com.fileshare.user.dto;

import com.fileshare.user.entity.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserResponseDto {

    private UUID id;
    private String email;
    private Role role;
    private LocalDateTime createdAt;

    public UserResponseDto() {
    }

    public UserResponseDto(UUID id, String email, Role role, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static UserResponseDtoBuilder builder() {
        return new UserResponseDtoBuilder();
    }

    public static class UserResponseDtoBuilder {
        private UUID id;
        private String email;
        private Role role;
        private LocalDateTime createdAt;

        public UserResponseDtoBuilder id(UUID id) { this.id = id; return this; }
        public UserResponseDtoBuilder email(String email) { this.email = email; return this; }
        public UserResponseDtoBuilder role(Role role) { this.role = role; return this; }
        public UserResponseDtoBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public UserResponseDto build() {
            return new UserResponseDto(id, email, role, createdAt);
        }
    }
}
