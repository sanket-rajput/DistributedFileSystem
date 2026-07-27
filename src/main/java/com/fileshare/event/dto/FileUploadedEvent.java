package com.fileshare.event.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class FileUploadedEvent {

    private UUID fileId;
    private UUID userId;
    private String filename;
    private long sizeBytes;
    private LocalDateTime timestamp = LocalDateTime.now();

    public FileUploadedEvent() {
    }

    public FileUploadedEvent(UUID fileId, UUID userId, String filename, long sizeBytes, LocalDateTime timestamp) {
        this.fileId = fileId;
        this.userId = userId;
        this.filename = filename;
        this.sizeBytes = sizeBytes;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    public UUID getFileId() { return fileId; }
    public void setFileId(UUID fileId) { this.fileId = fileId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
