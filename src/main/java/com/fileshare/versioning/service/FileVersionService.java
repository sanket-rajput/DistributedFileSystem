package com.fileshare.versioning.service;

import com.fileshare.file.service.FileService;
import com.fileshare.versioning.dto.FileVersionResponseDto;

import java.util.List;
import java.util.UUID;

public interface FileVersionService {

    List<FileVersionResponseDto> getVersionsForFile(UUID fileId, UUID ownerId);

    FileService.FileDownloadResult downloadFileVersion(UUID fileId, int versionNumber, UUID ownerId);

    FileVersionResponseDto restoreFileVersion(UUID fileId, int versionNumber, UUID ownerId);
}
