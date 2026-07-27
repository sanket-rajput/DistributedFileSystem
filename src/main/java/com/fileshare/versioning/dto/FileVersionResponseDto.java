package com.fileshare.versioning.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class FileVersionResponseDto {

    private UUID id;
    private UUID fileId;
    private int versionNumber;
    private String storageKey;
    private long sizeBytes;
    private String sha256Hash;
    private LocalDateTime createdAt;
    private UUID createdById;

    public FileVersionResponseDto() {
    }

    public FileVersionResponseDto(UUID id, UUID fileId, int versionNumber, String storageKey, long sizeBytes, String sha256Hash, LocalDateTime createdAt, UUID createdById) {
        this.id = id;
        this.fileId = fileId;
        this.versionNumber = versionNumber;
        this.storageKey = storageKey;
        this.sizeBytes = sizeBytes;
        this.sha256Hash = sha256Hash;
        this.createdAt = createdAt;
        this.createdById = createdById;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getFileId() { return fileId; }
    public void setFileId(UUID fileId) { this.fileId = fileId; }

    public int getVersionNumber() { return versionNumber; }
    public void setVersionNumber(int versionNumber) { this.versionNumber = versionNumber; }

    public String getStorageKey() { return storageKey; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }

    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }

    public String getSha256Hash() { return sha256Hash; }
    public void setSha256Hash(String sha256Hash) { this.sha256Hash = sha256Hash; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public UUID getCreatedById() { return createdById; }
    public void setCreatedById(UUID createdById) { this.createdById = createdById; }

    public static FileVersionResponseDtoBuilder builder() {
        return new FileVersionResponseDtoBuilder();
    }

    public static class FileVersionResponseDtoBuilder {
        private UUID id;
        private UUID fileId;
        private int versionNumber;
        private String storageKey;
        private long sizeBytes;
        private String sha256Hash;
        private LocalDateTime createdAt;
        private UUID createdById;

        public FileVersionResponseDtoBuilder id(UUID id) { this.id = id; return this; }
        public FileVersionResponseDtoBuilder fileId(UUID fileId) { this.fileId = fileId; return this; }
        public FileVersionResponseDtoBuilder versionNumber(int versionNumber) { this.versionNumber = versionNumber; return this; }
        public FileVersionResponseDtoBuilder storageKey(String storageKey) { this.storageKey = storageKey; return this; }
        public FileVersionResponseDtoBuilder sizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; return this; }
        public FileVersionResponseDtoBuilder sha256Hash(String sha256Hash) { this.sha256Hash = sha256Hash; return this; }
        public FileVersionResponseDtoBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public FileVersionResponseDtoBuilder createdById(UUID createdById) { this.createdById = createdById; return this; }

        public FileVersionResponseDto build() {
            return new FileVersionResponseDto(id, fileId, versionNumber, storageKey, sizeBytes, sha256Hash, createdAt, createdById);
        }
    }
}
