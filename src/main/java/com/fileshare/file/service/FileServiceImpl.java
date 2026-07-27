package com.fileshare.file.service;

import com.fileshare.audit.annotation.Auditable;
import com.fileshare.audit.entity.AuditAction;
import com.fileshare.common.exception.AccessDeniedException;
import com.fileshare.common.exception.BadRequestException;
import com.fileshare.common.exception.FileStorageException;
import com.fileshare.common.exception.ResourceNotFoundException;
import com.fileshare.event.dto.FileDeletedEvent;
import com.fileshare.event.dto.FileUploadedEvent;
import com.fileshare.event.service.EventPublisherService;
import com.fileshare.file.dto.FileResponseDto;
import com.fileshare.file.entity.FileMetadata;
import com.fileshare.file.mapper.FileMapper;
import com.fileshare.file.repository.FileMetadataRepository;
import com.fileshare.folder.entity.Folder;
import com.fileshare.folder.repository.FolderRepository;
import com.fileshare.search.specification.FileSpecification;
import com.fileshare.user.entity.User;
import com.fileshare.user.repository.UserRepository;
import com.fileshare.versioning.entity.FileVersion;
import com.fileshare.versioning.repository.FileVersionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {

    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    private final FileMetadataRepository fileMetadataRepository;
    private final FileVersionRepository fileVersionRepository;
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final FileMapper fileMapper;
    private final EventPublisherService eventPublisherService;
    private final io.micrometer.core.instrument.Counter deduplicatedUploadsCounter;
    private final io.micrometer.core.instrument.Timer fileUploadTimer;
    private final io.micrometer.core.instrument.Timer fileDownloadTimer;

    public FileServiceImpl(FileMetadataRepository fileMetadataRepository,
                           FileVersionRepository fileVersionRepository,
                           FolderRepository folderRepository,
                           UserRepository userRepository,
                           FileStorageService fileStorageService,
                           FileMapper fileMapper,
                           EventPublisherService eventPublisherService,
                           io.micrometer.core.instrument.Counter deduplicatedUploadsCounter,
                           io.micrometer.core.instrument.Timer fileUploadTimer,
                           io.micrometer.core.instrument.Timer fileDownloadTimer) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.fileVersionRepository = fileVersionRepository;
        this.folderRepository = folderRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.fileMapper = fileMapper;
        this.eventPublisherService = eventPublisherService;
        this.deduplicatedUploadsCounter = deduplicatedUploadsCounter;
        this.fileUploadTimer = fileUploadTimer;
        this.fileDownloadTimer = fileDownloadTimer;
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPLOAD)
    public FileResponseDto uploadFile(MultipartFile file, UUID folderId, UUID ownerId) {
        if (file.isEmpty()) {
            throw new BadRequestException("Cannot upload an empty file");
        }

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", ownerId));

        Folder folder = null;
        if (folderId != null) {
            folder = folderRepository.findById(folderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Folder", "id", folderId));

            if (!folder.getOwner().getId().equals(ownerId)) {
                throw new AccessDeniedException("You do not have permission to upload files to this folder");
            }
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "unnamed_file";
        }

        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        // Concurrent SHA-256 computation while reading stream
        String sha256Hash;
        byte[] buffer = new byte[8192];
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream is = file.getInputStream()) {
                while (is.read(buffer) != -1) {
                    // reading stream to compute digest
                }
            }
            sha256Hash = HexFormat.of().formatHex(digest.digest());
        } catch (Exception e) {
            log.error("Failed to compute SHA-256 hash: {}", e.getMessage(), e);
            throw new FileStorageException("Failed to compute file SHA-256 hash: " + e.getMessage(), e);
        }

        // Deduplication Check: Check if identical hash exists for this user
        Optional<FileMetadata> existingHashMatch = fileMetadataRepository.findFirstByOwnerIdAndSha256Hash(ownerId, sha256Hash);
        boolean isDeduplicated = existingHashMatch.isPresent();
        String storageKey;

        if (isDeduplicated) {
            deduplicatedUploadsCounter.increment();
            storageKey = existingHashMatch.get().getStorageKey();
            log.info("Deduplication triggered! Reusing S3 storageKey '{}' for file '{}'", storageKey, originalFilename);
        } else {
            storageKey = UUID.randomUUID().toString() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
            try (InputStream uploadStream = file.getInputStream()) {
                fileStorageService.uploadFile(uploadStream, file.getSize(), contentType, storageKey);
            } catch (Exception e) {
                log.error("Error streaming upload to S3: {}", e.getMessage(), e);
                throw new FileStorageException("Could not upload file bytes to S3: " + e.getMessage(), e);
            }
        }

        // Versioning Check: Uploading file with same name in same folder creates new FileVersion
        Optional<FileMetadata> existingNameMatch = (folderId != null)
                ? fileMetadataRepository.findByOwnerIdAndFolderIdAndOriginalFilename(ownerId, folderId, originalFilename)
                : fileMetadataRepository.findByOwnerIdAndFolderIsNullAndOriginalFilename(ownerId, originalFilename);

        FileMetadata metadataToSave;

        if (existingNameMatch.isPresent()) {
            FileMetadata existingFile = existingNameMatch.get();

            // Store current state as a FileVersion record before updating to new version
            FileVersion previousVersion = FileVersion.builder()
                    .fileMetadata(existingFile)
                    .versionNumber(existingFile.getCurrentVersionNumber())
                    .storageKey(existingFile.getStorageKey())
                    .sizeBytes(existingFile.getSizeBytes())
                    .sha256Hash(existingFile.getSha256Hash())
                    .createdBy(owner)
                    .build();
            fileVersionRepository.save(previousVersion);

            // Update existing file record to new version
            existingFile.setCurrentVersionNumber(existingFile.getCurrentVersionNumber() + 1);
            existingFile.setStorageKey(storageKey);
            existingFile.setSizeBytes(file.getSize());
            existingFile.setContentType(contentType);
            existingFile.setSha256Hash(sha256Hash);

            metadataToSave = fileMetadataRepository.save(existingFile);
            log.info("Created FileVersion v{} for existing file ID '{}'", metadataToSave.getCurrentVersionNumber(), metadataToSave.getId());
        } else {
            FileMetadata newFile = FileMetadata.builder()
                    .originalFilename(originalFilename)
                    .storageKey(storageKey)
                    .sizeBytes(file.getSize())
                    .contentType(contentType)
                    .sha256Hash(sha256Hash)
                    .currentVersionNumber(1)
                    .folder(folder)
                    .owner(owner)
                    .build();

            metadataToSave = fileMetadataRepository.save(newFile);

            // Create initial version entry (v1)
            FileVersion v1 = FileVersion.builder()
                    .fileMetadata(metadataToSave)
                    .versionNumber(1)
                    .storageKey(storageKey)
                    .sizeBytes(file.getSize())
                    .sha256Hash(sha256Hash)
                    .createdBy(owner)
                    .build();
            fileVersionRepository.save(v1);
        }

        // Fire-and-forget Kafka Event
        eventPublisherService.publishFileUploaded(new FileUploadedEvent(
                metadataToSave.getId(), ownerId, originalFilename, file.getSize(), LocalDateTime.now()
        ));

        FileResponseDto responseDto = fileMapper.toDto(metadataToSave);
        responseDto.setDeduplicated(isDeduplicated);
        return responseDto;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "fileMetadata", key = "#fileId")
    public FileResponseDto getFileMetadata(UUID fileId, UUID ownerId) {
        log.info("Cache miss for FileMetadata ID '{}'. Fetching from PostgreSQL...", fileId);
        FileMetadata fileMetadata = getFileEntityAndCheckOwner(fileId, ownerId);
        return fileMapper.toDto(fileMetadata);
    }

    @Override
    @Transactional(readOnly = true)
    @Auditable(action = AuditAction.DOWNLOAD)
    public FileDownloadResult downloadFile(UUID fileId, UUID ownerId) {
        FileMetadata fileMetadata = getFileEntityAndCheckOwner(fileId, ownerId);
        InputStream inputStream = fileStorageService.downloadFile(fileMetadata.getStorageKey());
        InputStreamResource resource = new InputStreamResource(inputStream);

        return new FileDownloadResult(
                resource,
                fileMetadata.getOriginalFilename(),
                fileMetadata.getContentType(),
                fileMetadata.getSizeBytes()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileResponseDto> listFiles(UUID folderId, UUID ownerId) {
        List<FileMetadata> files;
        if (folderId != null) {
            Folder folder = folderRepository.findById(folderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Folder", "id", folderId));

            if (!folder.getOwner().getId().equals(ownerId)) {
                throw new AccessDeniedException("You do not have access to this folder");
            }
            files = fileMetadataRepository.findByOwnerIdAndFolderId(ownerId, folderId);
        } else {
            files = fileMetadataRepository.findByOwnerIdAndFolderIsNull(ownerId);
        }

        return files.stream()
                .map(fileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FileResponseDto> searchFiles(String name, String contentType, UUID folderId, Long minSize, Long maxSize, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable, UUID ownerId) {
        Specification<FileMetadata> spec = FileSpecification.getSearchSpecification(
                name, contentType, folderId, minSize, maxSize, fromDate, toDate, ownerId
        );
        return fileMetadataRepository.findAll(spec, pageable).map(fileMapper::toDto);
    }

    @Override
    @Transactional
    @CacheEvict(value = "fileMetadata", key = "#fileId")
    @Auditable(action = AuditAction.DELETE)
    public void deleteFile(UUID fileId, UUID ownerId) {
        FileMetadata fileMetadata = getFileEntityAndCheckOwner(fileId, ownerId);

        // Delete version history
        List<FileVersion> versions = fileVersionRepository.findByFileMetadataIdOrderByVersionNumberDesc(fileId);
        fileVersionRepository.deleteAll(versions);

        // Check reference count before deleting physical S3 object
        long matchCount = fileMetadataRepository.findAll().stream()
                .filter(f -> f.getStorageKey().equals(fileMetadata.getStorageKey()))
                .count();

        if (matchCount <= 1) {
            try {
                fileStorageService.deleteFile(fileMetadata.getStorageKey());
            } catch (Exception e) {
                log.warn("Failed to delete S3 file with key '{}': {}", fileMetadata.getStorageKey(), e.getMessage());
            }
        }

        fileMetadataRepository.delete(fileMetadata);

        // Fire-and-forget Kafka Event
        eventPublisherService.publishFileDeleted(new FileDeletedEvent(
                fileId, ownerId, fileMetadata.getOriginalFilename(), LocalDateTime.now()
        ));
    }

    @Override
    @Transactional
    public void deleteFilesByFolder(UUID folderId) {
        List<FileMetadata> files = fileMetadataRepository.findByFolderId(folderId);
        for (FileMetadata file : files) {
            try {
                deleteFile(file.getId(), file.getOwner().getId());
            } catch (Exception e) {
                log.warn("Failed to delete file ID '{}' during folder purge: {}", file.getId(), e.getMessage());
            }
        }
    }

    private FileMetadata getFileEntityAndCheckOwner(UUID fileId, UUID ownerId) {
        FileMetadata fileMetadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", fileId));

        if (!fileMetadata.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("You do not have permission to access this file");
        }

        return fileMetadata;
    }
}
