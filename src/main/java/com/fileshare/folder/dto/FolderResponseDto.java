package com.fileshare.folder.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class FolderResponseDto {

    private UUID id;
    private String name;
    private UUID ownerId;
    private UUID parentFolderId;
    private LocalDateTime createdAt;

    public FolderResponseDto() {
    }

    public FolderResponseDto(UUID id, String name, UUID ownerId, UUID parentFolderId, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.parentFolderId = parentFolderId;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }

    public UUID getParentFolderId() { return parentFolderId; }
    public void setParentFolderId(UUID parentFolderId) { this.parentFolderId = parentFolderId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static FolderResponseDtoBuilder builder() {
        return new FolderResponseDtoBuilder();
    }

    public static class FolderResponseDtoBuilder {
        private UUID id;
        private String name;
        private UUID ownerId;
        private UUID parentFolderId;
        private LocalDateTime createdAt;

        public FolderResponseDtoBuilder id(UUID id) { this.id = id; return this; }
        public FolderResponseDtoBuilder name(String name) { this.name = name; return this; }
        public FolderResponseDtoBuilder ownerId(UUID ownerId) { this.ownerId = ownerId; return this; }
        public FolderResponseDtoBuilder parentFolderId(UUID parentFolderId) { this.parentFolderId = parentFolderId; return this; }
        public FolderResponseDtoBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public FolderResponseDto build() {
            return new FolderResponseDto(id, name, ownerId, parentFolderId, createdAt);
        }
    }
}
