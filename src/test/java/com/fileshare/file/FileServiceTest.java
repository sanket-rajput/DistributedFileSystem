package com.fileshare.file;

import com.fileshare.common.exception.AccessDeniedException;
import com.fileshare.event.service.EventPublisherService;
import com.fileshare.file.dto.FileResponseDto;
import com.fileshare.file.entity.FileMetadata;
import com.fileshare.file.mapper.FileMapper;
import com.fileshare.file.repository.FileMetadataRepository;
import com.fileshare.file.service.FileServiceImpl;
import com.fileshare.file.service.FileStorageService;
import com.fileshare.folder.repository.FolderRepository;
import com.fileshare.user.entity.User;
import com.fileshare.user.repository.UserRepository;
import com.fileshare.versioning.repository.FileVersionRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock private FileMetadataRepository fileMetadataRepository;
    @Mock private FileVersionRepository fileVersionRepository;
    @Mock private FolderRepository folderRepository;
    @Mock private UserRepository userRepository;
    @Mock private FileStorageService fileStorageService;
    @Mock private FileMapper fileMapper;
    @Mock private EventPublisherService eventPublisherService;
    @Mock private Counter deduplicatedUploadsCounter;
    @Mock private Timer fileUploadTimer;
    @Mock private Timer fileDownloadTimer;

    @InjectMocks
    private FileServiceImpl fileService;

    private User owner;
    private UUID ownerId;
    private FileMetadata sampleFile;
    private FileResponseDto sampleFileDto;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        owner = User.builder().id(ownerId).email("owner@example.com").build();
        sampleFile = FileMetadata.builder()
                .id(UUID.randomUUID())
                .originalFilename("test.pdf")
                .storageKey("key_test.pdf")
                .sizeBytes(1024)
                .contentType("application/pdf")
                .sha256Hash("hash123")
                .owner(owner)
                .build();

        sampleFileDto = FileResponseDto.builder()
                .id(sampleFile.getId())
                .originalFilename("test.pdf")
                .sizeBytes(1024)
                .ownerId(ownerId)
                .build();
    }

    @Test
    void uploadFile_HappyPath_NotDeduplicated() {
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.pdf", "application/pdf", "Hello World".getBytes());

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(fileMetadataRepository.findFirstByOwnerIdAndSha256Hash(eq(ownerId), anyString())).thenReturn(Optional.empty());
        when(fileMetadataRepository.findByOwnerIdAndFolderIsNullAndOriginalFilename(eq(ownerId), eq("test.pdf"))).thenReturn(Optional.empty());
        when(fileMetadataRepository.save(any(FileMetadata.class))).thenReturn(sampleFile);
        when(fileMapper.toDto(sampleFile)).thenReturn(sampleFileDto);

        FileResponseDto result = fileService.uploadFile(multipartFile, null, ownerId);

        assertNotNull(result);
        assertFalse(result.isDeduplicated());
        verify(fileStorageService, times(1)).uploadFile(any(), anyLong(), anyString(), anyString());
        verify(eventPublisherService, times(1)).publishFileUploaded(any());
    }

    @Test
    void uploadFile_DeduplicatedPath() {
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.pdf", "application/pdf", "Hello World".getBytes());

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(fileMetadataRepository.findFirstByOwnerIdAndSha256Hash(eq(ownerId), anyString())).thenReturn(Optional.of(sampleFile));
        when(fileMetadataRepository.findByOwnerIdAndFolderIsNullAndOriginalFilename(eq(ownerId), eq("test.pdf"))).thenReturn(Optional.empty());
        when(fileMetadataRepository.save(any(FileMetadata.class))).thenReturn(sampleFile);
        when(fileMapper.toDto(sampleFile)).thenReturn(sampleFileDto);

        FileResponseDto result = fileService.uploadFile(multipartFile, null, ownerId);

        assertNotNull(result);
        assertTrue(result.isDeduplicated());
        // Storage upload skipped on deduplication
        verify(fileStorageService, never()).uploadFile(any(), anyLong(), anyString(), anyString());
        verify(deduplicatedUploadsCounter, times(1)).increment();
    }

    @Test
    void getFileMetadata_OwnershipDenied_ThrowsException() {
        UUID strangerId = UUID.randomUUID();
        when(fileMetadataRepository.findById(sampleFile.getId())).thenReturn(Optional.of(sampleFile));

        assertThrows(AccessDeniedException.class, () -> fileService.getFileMetadata(sampleFile.getId(), strangerId));
    }
}
