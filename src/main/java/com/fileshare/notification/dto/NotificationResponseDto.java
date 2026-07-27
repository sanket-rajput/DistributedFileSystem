package com.fileshare.notification.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class NotificationResponseDto {

    private UUID id;
    private UUID userId;
    private String message;
    private String type;
    private boolean read;
    private LocalDateTime createdAt;

    public NotificationResponseDto() {
    }

    public NotificationResponseDto(UUID id, UUID userId, String message, String type, boolean read, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.type = type;
        this.read = read;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static NotificationResponseDtoBuilder builder() {
        return new NotificationResponseDtoBuilder();
    }

    public static class NotificationResponseDtoBuilder {
        private UUID id;
        private UUID userId;
        private String message;
        private String type;
        private boolean read;
        private LocalDateTime createdAt;

        public NotificationResponseDtoBuilder id(UUID id) { this.id = id; return this; }
        public NotificationResponseDtoBuilder userId(UUID userId) { this.userId = userId; return this; }
        public NotificationResponseDtoBuilder message(String message) { this.message = message; return this; }
        public NotificationResponseDtoBuilder type(String type) { this.type = type; return this; }
        public NotificationResponseDtoBuilder read(boolean read) { this.read = read; return this; }
        public NotificationResponseDtoBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public NotificationResponseDto build() {
            return new NotificationResponseDto(id, userId, message, type, read, createdAt);
        }
    }
}
