package com.fileshare.sharing.entity;

import com.fileshare.file.entity.FileMetadata;
import com.fileshare.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shares")
public class Share {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_metadata_id", nullable = false)
    private FileMetadata fileMetadata;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SharePermission permission;

    @Column(nullable = false)
    private boolean revoked = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Share() {
    }

    public Share(UUID id, FileMetadata fileMetadata, String token, User createdBy, LocalDateTime expiresAt, SharePermission permission, boolean revoked, LocalDateTime createdAt) {
        this.id = id;
        this.fileMetadata = fileMetadata;
        this.token = token;
        this.createdBy = createdBy;
        this.expiresAt = expiresAt;
        this.permission = permission;
        this.revoked = revoked;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public FileMetadata getFileMetadata() { return fileMetadata; }
    public void setFileMetadata(FileMetadata fileMetadata) { this.fileMetadata = fileMetadata; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public SharePermission getPermission() { return permission; }
    public void setPermission(SharePermission permission) { this.permission = permission; }

    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public static ShareBuilder builder() {
        return new ShareBuilder();
    }

    public static class ShareBuilder {
        private UUID id;
        private FileMetadata fileMetadata;
        private String token;
        private User createdBy;
        private LocalDateTime expiresAt;
        private SharePermission permission;
        private boolean revoked = false;
        private LocalDateTime createdAt;

        public ShareBuilder id(UUID id) { this.id = id; return this; }
        public ShareBuilder fileMetadata(FileMetadata fileMetadata) { this.fileMetadata = fileMetadata; return this; }
        public ShareBuilder token(String token) { this.token = token; return this; }
        public ShareBuilder createdBy(User createdBy) { this.createdBy = createdBy; return this; }
        public ShareBuilder expiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; return this; }
        public ShareBuilder permission(SharePermission permission) { this.permission = permission; return this; }
        public ShareBuilder revoked(boolean revoked) { this.revoked = revoked; return this; }
        public ShareBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Share build() {
            return new Share(id, fileMetadata, token, createdBy, expiresAt, permission, revoked, createdAt);
        }
    }
}
