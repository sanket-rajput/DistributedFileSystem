package com.fileshare.sharing;

import com.fileshare.common.exception.AccessDeniedException;

import com.fileshare.event.service.EventPublisherService;
import com.fileshare.file.entity.FileMetadata;
import com.fileshare.file.repository.FileMetadataRepository;
import com.fileshare.file.service.FileStorageService;
import com.fileshare.sharing.dto.CreateShareRequest;
import com.fileshare.sharing.dto.ShareResponseDto;
import com.fileshare.sharing.entity.Share;
import com.fileshare.sharing.entity.SharePermission;
import com.fileshare.sharing.mapper.ShareMapper;
import com.fileshare.sharing.repository.ShareRepository;
import com.fileshare.sharing.service.SharingServiceImpl;
import com.fileshare.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SharingServiceTest {

    @Mock private ShareRepository shareRepository;
    @Mock private FileMetadataRepository fileMetadataRepository;
    @Mock private FileStorageService fileStorageService;
    @Mock private ShareMapper shareMapper;
    @Mock private EventPublisherService eventPublisherService;

    @InjectMocks
    private SharingServiceImpl sharingService;

    private User owner;
    private UUID ownerId;
    private FileMetadata fileMetadata;
    private Share share;
    private ShareResponseDto shareDto;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        owner = User.builder().id(ownerId).email("owner@example.com").build();
        fileMetadata = FileMetadata.builder()
                .id(UUID.randomUUID())
                .originalFilename("report.docx")
                .owner(owner)
                .build();

        share = Share.builder()
                .id(UUID.randomUUID())
                .fileMetadata(fileMetadata)
                .token("tok123")
                .createdBy(owner)
                .permission(SharePermission.DOWNLOAD)
                .revoked(false)
                .build();

        shareDto = ShareResponseDto.builder()
                .id(share.getId())
                .token("tok123")
                .permission(SharePermission.DOWNLOAD)
                .build();
    }

    @Test
    void createShare_Success() {
        CreateShareRequest req = new CreateShareRequest(LocalDateTime.now().plusDays(1), SharePermission.DOWNLOAD);

        when(fileMetadataRepository.findById(fileMetadata.getId())).thenReturn(Optional.of(fileMetadata));
        when(shareRepository.save(any(Share.class))).thenReturn(share);
        when(shareMapper.toDto(share)).thenReturn(shareDto);

        ShareResponseDto res = sharingService.createShare(fileMetadata.getId(), req, ownerId);

        assertNotNull(res);
        assertEquals("tok123", res.getToken());
        verify(eventPublisherService, times(1)).publishFileShared(any());
    }

    @Test
    void getShareByToken_Revoked_ThrowsException() {
        share.setRevoked(true);
        when(shareRepository.findByToken("tok123")).thenReturn(Optional.of(share));

        assertThrows(AccessDeniedException.class, () -> sharingService.getShareByToken("tok123"));
    }

    @Test
    void getShareByToken_Expired_ThrowsException() {
        share.setExpiresAt(LocalDateTime.now().minusHours(1));
        when(shareRepository.findByToken("tok123")).thenReturn(Optional.of(share));

        assertThrows(AccessDeniedException.class, () -> sharingService.getShareByToken("tok123"));
    }
}
