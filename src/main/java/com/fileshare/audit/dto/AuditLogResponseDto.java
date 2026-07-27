package com.fileshare.audit.dto;

import com.fileshare.audit.entity.AuditAction;

import java.time.LocalDateTime;
import java.util.UUID;

public class AuditLogResponseDto {

    private UUID id;
    private UUID userId;
    private String userEmail;
    private AuditAction action;
    private String resourceType;
    private UUID resourceId;
    private LocalDateTime timestamp;
    private String details;

    public AuditLogResponseDto() {
    }

    public AuditLogResponseDto(UUID id, UUID userId, String userEmail, AuditAction action, String resourceType, UUID resourceId, LocalDateTime timestamp, String details) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.timestamp = timestamp;
        this.details = details;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public AuditAction getAction() { return action; }
    public void setAction(AuditAction action) { this.action = action; }

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }

    public UUID getResourceId() { return resourceId; }
    public void setResourceId(UUID resourceId) { this.resourceId = resourceId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public static AuditLogResponseDtoBuilder builder() {
        return new AuditLogResponseDtoBuilder();
    }

    public static class AuditLogResponseDtoBuilder {
        private UUID id;
        private UUID userId;
        private String userEmail;
        private AuditAction action;
        private String resourceType;
        private UUID resourceId;
        private LocalDateTime timestamp;
        private String details;

        public AuditLogResponseDtoBuilder id(UUID id) { this.id = id; return this; }
        public AuditLogResponseDtoBuilder userId(UUID userId) { this.userId = userId; return this; }
        public AuditLogResponseDtoBuilder userEmail(String userEmail) { this.userEmail = userEmail; return this; }
        public AuditLogResponseDtoBuilder action(AuditAction action) { this.action = action; return this; }
        public AuditLogResponseDtoBuilder resourceType(String resourceType) { this.resourceType = resourceType; return this; }
        public AuditLogResponseDtoBuilder resourceId(UUID resourceId) { this.resourceId = resourceId; return this; }
        public AuditLogResponseDtoBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
        public AuditLogResponseDtoBuilder details(String details) { this.details = details; return this; }

        public AuditLogResponseDto build() {
            return new AuditLogResponseDto(id, userId, userEmail, action, resourceType, resourceId, timestamp, details);
        }
    }
}
