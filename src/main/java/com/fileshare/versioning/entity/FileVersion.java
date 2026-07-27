package com.fileshare.versioning.entity;

import com.fileshare.file.entity.FileMetadata;
import com.fileshare.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "file_versions")
public class FileVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_metadata_id", nullable = false)
    private FileMetadata fileMetadata;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "sha256_hash")
    private String sha256Hash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    public FileVersion() {
    }

    public FileVersion(UUID id, FileMetadata fileMetadata, int versionNumber, String storageKey, long sizeBytes, String sha256Hash, LocalDateTime createdAt, User createdBy) {
        this.id = id;
        this.fileMetadata = fileMetadata;
        this.versionNumber = versionNumber;
        this.storageKey = storageKey;
        this.sizeBytes = sizeBytes;
        this.sha256Hash = sha256Hash;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
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

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public static FileVersionBuilder builder() {
        return new FileVersionBuilder();
    }

    public static class FileVersionBuilder {
        private UUID id;
        private FileMetadata fileMetadata;
        private int versionNumber;
        private String storageKey;
        private long sizeBytes;
        private String sha256Hash;
        private LocalDateTime createdAt;
        private User createdBy;

        public FileVersionBuilder id(UUID id) { this.id = id; return this; }
        public FileVersionBuilder fileMetadata(FileMetadata fileMetadata) { this.fileMetadata = fileMetadata; return this; }
        public FileVersionBuilder versionNumber(int versionNumber) { this.versionNumber = versionNumber; return this; }
        public FileVersionBuilder storageKey(String storageKey) { this.storageKey = storageKey; return this; }
        public FileVersionBuilder sizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; return this; }
        public FileVersionBuilder sha256Hash(String sha256Hash) { this.sha256Hash = sha256Hash; return this; }
        public FileVersionBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public FileVersionBuilder createdBy(User createdBy) { this.createdBy = createdBy; return this; }

        public FileVersion build() {
            return new FileVersion(id, fileMetadata, versionNumber, storageKey, sizeBytes, sha256Hash, createdAt, createdBy);
        }
    }
}
