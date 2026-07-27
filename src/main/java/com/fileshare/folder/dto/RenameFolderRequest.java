package com.fileshare.folder.dto;

import jakarta.validation.constraints.NotBlank;

public class RenameFolderRequest {

    @NotBlank(message = "New folder name is required")
    private String name;

    public RenameFolderRequest() {
    }

    public RenameFolderRequest(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public static RenameFolderRequestBuilder builder() {
        return new RenameFolderRequestBuilder();
    }

    public static class RenameFolderRequestBuilder {
        private String name;

        public RenameFolderRequestBuilder name(String name) { this.name = name; return this; }

        public RenameFolderRequest build() {
            return new RenameFolderRequest(name);
        }
    }
}
