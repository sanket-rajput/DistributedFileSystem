package com.fileshare.folder.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public class CreateFolderRequest {

    @NotBlank(message = "Folder name is required")
    private String name;

    private UUID parentFolderId;

    public CreateFolderRequest() {
    }

    public CreateFolderRequest(String name, UUID parentFolderId) {
        this.name = name;
        this.parentFolderId = parentFolderId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public UUID getParentFolderId() { return parentFolderId; }
    public void setParentFolderId(UUID parentFolderId) { this.parentFolderId = parentFolderId; }

    public static CreateFolderRequestBuilder builder() {
        return new CreateFolderRequestBuilder();
    }

    public static class CreateFolderRequestBuilder {
        private String name;
        private UUID parentFolderId;

        public CreateFolderRequestBuilder name(String name) { this.name = name; return this; }
        public CreateFolderRequestBuilder parentFolderId(UUID parentFolderId) { this.parentFolderId = parentFolderId; return this; }

        public CreateFolderRequest build() {
            return new CreateFolderRequest(name, parentFolderId);
        }
    }
}
