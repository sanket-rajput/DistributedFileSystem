package com.fileshare.event.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class FileSharedEvent {

    private UUID fileId;
    private UUID userId;
    private String shareToken;
    private String permission;
    private LocalDateTime timestamp = LocalDateTime.now();

    public FileSharedEvent() {
    }

    public FileSharedEvent(UUID fileId, UUID userId, String shareToken, String permission, LocalDateTime timestamp) {
        this.fileId = fileId;
        this.userId = userId;
        this.shareToken = shareToken;
        this.permission = permission;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    public UUID getFileId() { return fileId; }
    public void setFileId(UUID fileId) { this.fileId = fileId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getShareToken() { return shareToken; }
    public void setShareToken(String shareToken) { this.shareToken = shareToken; }

    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
