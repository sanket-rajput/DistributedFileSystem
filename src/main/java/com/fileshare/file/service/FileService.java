package com.fileshare.file.service;

import com.fileshare.file.dto.FileResponseDto;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface FileService {

    FileResponseDto uploadFile(MultipartFile file, UUID folderId, UUID ownerId);

    FileResponseDto getFileMetadata(UUID fileId, UUID ownerId);

    FileDownloadResult downloadFile(UUID fileId, UUID ownerId);

    List<FileResponseDto> listFiles(UUID folderId, UUID ownerId);

    Page<FileResponseDto> searchFiles(String name, String contentType, UUID folderId, Long minSize, Long maxSize, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable, UUID ownerId);

    void deleteFile(UUID fileId, UUID ownerId);

    void deleteFilesByFolder(UUID folderId);

    record FileDownloadResult(Resource resource, String filename, String contentType, long sizeBytes) {}
}
