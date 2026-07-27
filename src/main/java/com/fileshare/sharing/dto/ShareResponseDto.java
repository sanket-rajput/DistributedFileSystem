package com.fileshare.sharing.dto;

import com.fileshare.file.dto.FileResponseDto;
import com.fileshare.sharing.entity.SharePermission;

import java.time.LocalDateTime;
import java.util.UUID;

public class ShareResponseDto {

    private UUID id;
    private FileResponseDto file;
    private String token;
    private String shareUrl;
    private LocalDateTime expiresAt;
    private SharePermission permission;
    private boolean revoked;
    private LocalDateTime createdAt;
    private UUID createdById;

    public ShareResponseDto() {
    }

    public ShareResponseDto(UUID id, FileResponseDto file, String token, String shareUrl, LocalDateTime expiresAt, SharePermission permission, boolean revoked, LocalDateTime createdAt, UUID createdById) {
        this.id = id;
        this.file = file;
        this.token = token;
        this.shareUrl = shareUrl;
        this.expiresAt = expiresAt;
        this.permission = permission;
        this.revoked = revoked;
        this.createdAt = createdAt;
        this.createdById = createdById;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public FileResponseDto getFile() { return file; }
    public void setFile(FileResponseDto file) { this.file = file; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getShareUrl() { return shareUrl; }
    public void setShareUrl(String shareUrl) { this.shareUrl = shareUrl; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public SharePermission getPermission() { return permission; }
    public void setPermission(SharePermission permission) { this.permission = permission; }

    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public UUID getCreatedById() { return createdById; }
    public void setCreatedById(UUID createdById) { this.createdById = createdById; }

    public static ShareResponseDtoBuilder builder() {
        return new ShareResponseDtoBuilder();
    }

    public static class ShareResponseDtoBuilder {
        private UUID id;
        private FileResponseDto file;
        private String token;
        private String shareUrl;
        private LocalDateTime expiresAt;
        private SharePermission permission;
        private boolean revoked;
        private LocalDateTime createdAt;
        private UUID createdById;

        public ShareResponseDtoBuilder id(UUID id) { this.id = id; return this; }
        public ShareResponseDtoBuilder file(FileResponseDto file) { this.file = file; return this; }
        public ShareResponseDtoBuilder token(String token) { this.token = token; return this; }
        public ShareResponseDtoBuilder shareUrl(String shareUrl) { this.shareUrl = shareUrl; return this; }
        public ShareResponseDtoBuilder expiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; return this; }
        public ShareResponseDtoBuilder permission(SharePermission permission) { this.permission = permission; return this; }
        public ShareResponseDtoBuilder revoked(boolean revoked) { this.revoked = revoked; return this; }
        public ShareResponseDtoBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public ShareResponseDtoBuilder createdById(UUID createdById) { this.createdById = createdById; return this; }

        public ShareResponseDto build() {
            return new ShareResponseDto(id, file, token, shareUrl, expiresAt, permission, revoked, createdAt, createdById);
        }
    }
}
