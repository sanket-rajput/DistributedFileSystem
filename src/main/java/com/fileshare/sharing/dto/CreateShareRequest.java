package com.fileshare.sharing.dto;

import com.fileshare.sharing.entity.SharePermission;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class CreateShareRequest {

    private LocalDateTime expiresAt; // Nullable

    @NotNull(message = "Share permission is required")
    private SharePermission permission = SharePermission.DOWNLOAD;

    public CreateShareRequest() {
    }

    public CreateShareRequest(LocalDateTime expiresAt, SharePermission permission) {
        this.expiresAt = expiresAt;
        this.permission = permission != null ? permission : SharePermission.DOWNLOAD;
    }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public SharePermission getPermission() { return permission; }
    public void setPermission(SharePermission permission) { this.permission = permission; }

    public static CreateShareRequestBuilder builder() {
        return new CreateShareRequestBuilder();
    }

    public static class CreateShareRequestBuilder {
        private LocalDateTime expiresAt;
        private SharePermission permission = SharePermission.DOWNLOAD;

        public CreateShareRequestBuilder expiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; return this; }
        public CreateShareRequestBuilder permission(SharePermission permission) { this.permission = permission; return this; }

        public CreateShareRequest build() {
            return new CreateShareRequest(expiresAt, permission);
        }
    }
}
