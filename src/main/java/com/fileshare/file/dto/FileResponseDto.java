package com.fileshare.file.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class FileResponseDto {

    private UUID id;
    private String originalFilename;
    private String storageKey;
    private long sizeBytes;
    private String contentType;
    private String sha256Hash;
    private int currentVersionNumber;
    private boolean isDeduplicated;
    private UUID folderId;
    private UUID ownerId;
    private LocalDateTime createdAt;

    public FileResponseDto() {
    }

    public FileResponseDto(UUID id, String originalFilename, String storageKey, long sizeBytes, String contentType, String sha256Hash, int currentVersionNumber, boolean isDeduplicated, UUID folderId, UUID ownerId, LocalDateTime createdAt) {
        this.id = id;
        this.originalFilename = originalFilename;
        this.storageKey = storageKey;
        this.sizeBytes = sizeBytes;
        this.contentType = contentType;
        this.sha256Hash = sha256Hash;
        this.currentVersionNumber = currentVersionNumber;
        this.isDeduplicated = isDeduplicated;
        this.folderId = folderId;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getStorageKey() { return storageKey; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }

    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getSha256Hash() { return sha256Hash; }
    public void setSha256Hash(String sha256Hash) { this.sha256Hash = sha256Hash; }

    public int getCurrentVersionNumber() { return currentVersionNumber; }
    public void setCurrentVersionNumber(int currentVersionNumber) { this.currentVersionNumber = currentVersionNumber; }

    public boolean isDeduplicated() { return isDeduplicated; }
    public void setDeduplicated(boolean deduplicated) { isDeduplicated = deduplicated; }

    public UUID getFolderId() { return folderId; }
    public void setFolderId(UUID folderId) { this.folderId = folderId; }

    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static FileResponseDtoBuilder builder() {
        return new FileResponseDtoBuilder();
    }

    public static class FileResponseDtoBuilder {
        private UUID id;
        private String originalFilename;
        private String storageKey;
        private long sizeBytes;
        private String contentType;
        private String sha256Hash;
        private int currentVersionNumber;
        private boolean isDeduplicated;
        private UUID folderId;
        private UUID ownerId;
        private LocalDateTime createdAt;

        public FileResponseDtoBuilder id(UUID id) { this.id = id; return this; }
        public FileResponseDtoBuilder originalFilename(String originalFilename) { this.originalFilename = originalFilename; return this; }
        public FileResponseDtoBuilder storageKey(String storageKey) { this.storageKey = storageKey; return this; }
        public FileResponseDtoBuilder sizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; return this; }
        public FileResponseDtoBuilder contentType(String contentType) { this.contentType = contentType; return this; }
        public FileResponseDtoBuilder sha256Hash(String sha256Hash) { this.sha256Hash = sha256Hash; return this; }
        public FileResponseDtoBuilder currentVersionNumber(int currentVersionNumber) { this.currentVersionNumber = currentVersionNumber; return this; }
        public FileResponseDtoBuilder deduplicated(boolean deduplicated) { isDeduplicated = deduplicated; return this; }
        public FileResponseDtoBuilder folderId(UUID folderId) { this.folderId = folderId; return this; }
        public FileResponseDtoBuilder ownerId(UUID ownerId) { this.ownerId = ownerId; return this; }
        public FileResponseDtoBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public FileResponseDto build() {
            return new FileResponseDto(id, originalFilename, storageKey, sizeBytes, contentType, sha256Hash, currentVersionNumber, isDeduplicated, folderId, ownerId, createdAt);
        }
    }
}
