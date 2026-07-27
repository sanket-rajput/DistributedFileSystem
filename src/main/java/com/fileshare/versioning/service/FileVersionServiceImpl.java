package com.fileshare.versioning.service;

import com.fileshare.common.exception.AccessDeniedException;
import com.fileshare.common.exception.ResourceNotFoundException;
import com.fileshare.file.entity.FileMetadata;
import com.fileshare.file.repository.FileMetadataRepository;
import com.fileshare.file.service.FileService;
import com.fileshare.file.service.FileStorageService;
import com.fileshare.versioning.dto.FileVersionResponseDto;
import com.fileshare.versioning.entity.FileVersion;
import com.fileshare.versioning.mapper.FileVersionMapper;
import com.fileshare.versioning.repository.FileVersionRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileVersionServiceImpl implements FileVersionService {

    private final FileVersionRepository fileVersionRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final FileStorageService fileStorageService;
    private final FileVersionMapper fileVersionMapper;

    public FileVersionServiceImpl(FileVersionRepository fileVersionRepository,
                                  FileMetadataRepository fileMetadataRepository,
                                  FileStorageService fileStorageService,
                                  FileVersionMapper fileVersionMapper) {
        this.fileVersionRepository = fileVersionRepository;
        this.fileMetadataRepository = fileMetadataRepository;
        this.fileStorageService = fileStorageService;
        this.fileVersionMapper = fileVersionMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileVersionResponseDto> getVersionsForFile(UUID fileId, UUID ownerId) {
        FileMetadata fileMetadata = getFileEntityAndCheckOwner(fileId, ownerId);
        return fileVersionRepository.findByFileMetadataIdOrderByVersionNumberDesc(fileMetadata.getId()).stream()
                .map(fileVersionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FileService.FileDownloadResult downloadFileVersion(UUID fileId, int versionNumber, UUID ownerId) {
        FileMetadata fileMetadata = getFileEntityAndCheckOwner(fileId, ownerId);
        FileVersion version = fileVersionRepository.findByFileMetadataIdAndVersionNumber(fileMetadata.getId(), versionNumber)
                .orElseThrow(() -> new ResourceNotFoundException("FileVersion", "versionNumber", versionNumber));

        InputStream inputStream = fileStorageService.downloadFile(version.getStorageKey());
        InputStreamResource resource = new InputStreamResource(inputStream);

        return new FileService.FileDownloadResult(
                resource,
                fileMetadata.getOriginalFilename() + ".v" + versionNumber,
                fileMetadata.getContentType(),
                version.getSizeBytes()
        );
    }

    @Override
    @Transactional
    @CacheEvict(value = "fileMetadata", key = "#fileId")
    public FileVersionResponseDto restoreFileVersion(UUID fileId, int versionNumber, UUID ownerId) {
        FileMetadata fileMetadata = getFileEntityAndCheckOwner(fileId, ownerId);
        FileVersion targetVersion = fileVersionRepository.findByFileMetadataIdAndVersionNumber(fileMetadata.getId(), versionNumber)
                .orElseThrow(() -> new ResourceNotFoundException("FileVersion", "versionNumber", versionNumber));

        // Create a new version representing the restoration to preserve history
        int nextVersionNumber = fileMetadata.getCurrentVersionNumber() + 1;
        FileVersion restoredVersion = FileVersion.builder()
                .fileMetadata(fileMetadata)
                .versionNumber(nextVersionNumber)
                .storageKey(targetVersion.getStorageKey())
                .sizeBytes(targetVersion.getSizeBytes())
                .sha256Hash(targetVersion.getSha256Hash())
                .createdBy(fileMetadata.getOwner())
                .build();

        FileVersion savedVersion = fileVersionRepository.save(restoredVersion);

        // Update FileMetadata to point to restored version
        fileMetadata.setStorageKey(targetVersion.getStorageKey());
        fileMetadata.setSizeBytes(targetVersion.getSizeBytes());
        fileMetadata.setSha256Hash(targetVersion.getSha256Hash());
        fileMetadata.setCurrentVersionNumber(nextVersionNumber);
        fileMetadataRepository.save(fileMetadata);

        return fileVersionMapper.toDto(savedVersion);
    }

    private FileMetadata getFileEntityAndCheckOwner(UUID fileId, UUID ownerId) {
        FileMetadata fileMetadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", fileId));

        if (!fileMetadata.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("You do not have permission to access version history for this file");
        }

        return fileMetadata;
    }
}
