package com.fileshare.sharing.service;

import com.fileshare.common.exception.AccessDeniedException;
import com.fileshare.common.exception.BadRequestException;
import com.fileshare.common.exception.ResourceNotFoundException;
import com.fileshare.event.dto.FileSharedEvent;
import com.fileshare.event.service.EventPublisherService;
import com.fileshare.file.entity.FileMetadata;
import com.fileshare.file.repository.FileMetadataRepository;
import com.fileshare.file.service.FileService;
import com.fileshare.file.service.FileStorageService;
import com.fileshare.sharing.dto.CreateShareRequest;
import com.fileshare.sharing.dto.ShareResponseDto;
import com.fileshare.sharing.entity.Share;
import com.fileshare.sharing.entity.SharePermission;
import com.fileshare.sharing.mapper.ShareMapper;
import com.fileshare.sharing.repository.ShareRepository;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SharingServiceImpl implements SharingService {

    private final ShareRepository shareRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final FileStorageService fileStorageService;
    private final ShareMapper shareMapper;
    private final EventPublisherService eventPublisherService;

    public SharingServiceImpl(ShareRepository shareRepository,
                              FileMetadataRepository fileMetadataRepository,
                              FileStorageService fileStorageService,
                              ShareMapper shareMapper,
                              EventPublisherService eventPublisherService) {
        this.shareRepository = shareRepository;
        this.fileMetadataRepository = fileMetadataRepository;
        this.fileStorageService = fileStorageService;
        this.shareMapper = shareMapper;
        this.eventPublisherService = eventPublisherService;
    }

    @Override
    @Transactional
    public ShareResponseDto createShare(UUID fileId, CreateShareRequest request, UUID ownerId) {
        FileMetadata fileMetadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", fileId));

        if (!fileMetadata.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("You do not have permission to share this file");
        }

        if (request.getExpiresAt() != null && request.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Expiration time cannot be in the past");
        }

        String token = UUID.randomUUID().toString().replace("-", "");

        Share share = Share.builder()
                .fileMetadata(fileMetadata)
                .token(token)
                .createdBy(fileMetadata.getOwner())
                .expiresAt(request.getExpiresAt())
                .permission(request.getPermission())
                .revoked(false)
                .build();

        Share savedShare = shareRepository.save(share);

        // Fire-and-forget Kafka event
        eventPublisherService.publishFileShared(new FileSharedEvent(
                fileMetadata.getId(), ownerId, token, request.getPermission().name(), LocalDateTime.now()
        ));

        ShareResponseDto dto = shareMapper.toDto(savedShare);
        dto.setShareUrl("/api/v1/share/" + token);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public ShareResponseDto getShareByToken(String token) {
        Share share = validateAndGetShare(token);
        ShareResponseDto dto = shareMapper.toDto(share);
        dto.setShareUrl("/api/v1/share/" + token);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public FileService.FileDownloadResult downloadSharedFile(String token) {
        Share share = validateAndGetShare(token);

        if (share.getPermission() != SharePermission.DOWNLOAD && share.getPermission() != SharePermission.VIEW) {
            throw new AccessDeniedException("The share link does not grant download access");
        }

        FileMetadata metadata = share.getFileMetadata();
        InputStream inputStream = fileStorageService.downloadFile(metadata.getStorageKey());
        InputStreamResource resource = new InputStreamResource(inputStream);

        return new FileService.FileDownloadResult(
                resource,
                metadata.getOriginalFilename(),
                metadata.getContentType(),
                metadata.getSizeBytes()
        );
    }

    @Override
    @Transactional
    public void revokeShare(UUID fileId, UUID shareId, UUID ownerId) {
        Share share = shareRepository.findByIdAndFileMetadataOwnerId(shareId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Share", "id", shareId));

        if (!share.getFileMetadata().getId().equals(fileId)) {
            throw new BadRequestException("Share does not belong to the specified file");
        }

        share.setRevoked(true);
        shareRepository.save(share);
    }

    private Share validateAndGetShare(String token) {
        Share share = shareRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Shared Link", "token", token));

        if (share.isRevoked()) {
            throw new AccessDeniedException("This share link has been revoked");
        }

        if (share.isExpired()) {
            throw new AccessDeniedException("This share link has expired");
        }

        return share;
    }
}
