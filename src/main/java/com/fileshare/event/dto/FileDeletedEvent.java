package com.fileshare.event.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class FileDeletedEvent {

    private UUID fileId;
    private UUID userId;
    private String filename;
    private LocalDateTime timestamp = LocalDateTime.now();

    public FileDeletedEvent() {
    }

    public FileDeletedEvent(UUID fileId, UUID userId, String filename, LocalDateTime timestamp) {
        this.fileId = fileId;
        this.userId = userId;
        this.filename = filename;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    public UUID getFileId() { return fileId; }
    public void setFileId(UUID fileId) { this.fileId = fileId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
