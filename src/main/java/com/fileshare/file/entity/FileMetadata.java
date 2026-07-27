package com.fileshare.file.entity;

import com.fileshare.folder.entity.Folder;
import com.fileshare.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "file_metadata", indexes = {
        @Index(name = "idx_file_sha256", columnList = "sha256_hash")
})
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "sha256_hash")
    private String sha256Hash;

    @Column(name = "current_version_number", nullable = false)
    private int currentVersionNumber = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private Folder folder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public FileMetadata() {
    }

    public FileMetadata(UUID id, String originalFilename, String storageKey, long sizeBytes, String contentType, String sha256Hash, int currentVersionNumber, Folder folder, User owner, LocalDateTime createdAt) {
        this.id = id;
        this.originalFilename = originalFilename;
        this.storageKey = storageKey;
        this.sizeBytes = sizeBytes;
        this.contentType = contentType;
        this.sha256Hash = sha256Hash;
        this.currentVersionNumber = currentVersionNumber > 0 ? currentVersionNumber : 1;
        this.folder = folder;
        this.owner = owner;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (currentVersionNumber <= 0) {
            currentVersionNumber = 1;
        }
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

    public Folder getFolder() { return folder; }
    public void setFolder(Folder folder) { this.folder = folder; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static FileMetadataBuilder builder() {
        return new FileMetadataBuilder();
    }

    public static class FileMetadataBuilder {
        private UUID id;
        private String originalFilename;
        private String storageKey;
        private long sizeBytes;
        private String contentType;
        private String sha256Hash;
        private int currentVersionNumber = 1;
        private Folder folder;
        private User owner;
        private LocalDateTime createdAt;

        public FileMetadataBuilder id(UUID id) { this.id = id; return this; }
        public FileMetadataBuilder originalFilename(String originalFilename) { this.originalFilename = originalFilename; return this; }
        public FileMetadataBuilder storageKey(String storageKey) { this.storageKey = storageKey; return this; }
        public FileMetadataBuilder sizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; return this; }
        public FileMetadataBuilder contentType(String contentType) { this.contentType = contentType; return this; }
        public FileMetadataBuilder sha256Hash(String sha256Hash) { this.sha256Hash = sha256Hash; return this; }
        public FileMetadataBuilder currentVersionNumber(int currentVersionNumber) { this.currentVersionNumber = currentVersionNumber; return this; }
        public FileMetadataBuilder folder(Folder folder) { this.folder = folder; return this; }
        public FileMetadataBuilder owner(User owner) { this.owner = owner; return this; }
        public FileMetadataBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public FileMetadata build() {
            return new FileMetadata(id, originalFilename, storageKey, sizeBytes, contentType, sha256Hash, currentVersionNumber, folder, owner, createdAt);
        }
    }
}
