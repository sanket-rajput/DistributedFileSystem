package com.fileshare.versioning;

import com.fileshare.common.exception.ResourceNotFoundException;
import com.fileshare.file.entity.FileMetadata;
import com.fileshare.file.repository.FileMetadataRepository;
import com.fileshare.file.service.FileStorageService;
import com.fileshare.user.entity.User;
import com.fileshare.versioning.dto.FileVersionResponseDto;
import com.fileshare.versioning.entity.FileVersion;
import com.fileshare.versioning.mapper.FileVersionMapper;
import com.fileshare.versioning.repository.FileVersionRepository;
import com.fileshare.versioning.service.FileVersionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileVersionServiceTest {

    @Mock private FileVersionRepository fileVersionRepository;
    @Mock private FileMetadataRepository fileMetadataRepository;
    @Mock private FileStorageService fileStorageService;
    @Mock private FileVersionMapper fileVersionMapper;

    @InjectMocks
    private FileVersionServiceImpl fileVersionService;

    private User owner;
    private UUID ownerId;
    private FileMetadata fileMetadata;
    private FileVersion version1;
    private FileVersionResponseDto versionDto;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        owner = User.builder().id(ownerId).email("owner@example.com").build();
        fileMetadata = FileMetadata.builder()
                .id(UUID.randomUUID())
                .originalFilename("doc.pdf")
                .storageKey("key_v2")
                .sizeBytes(200)
                .sha256Hash("hash2")
                .currentVersionNumber(2)
                .owner(owner)
                .build();

        version1 = FileVersion.builder()
                .id(UUID.randomUUID())
                .fileMetadata(fileMetadata)
                .versionNumber(1)
                .storageKey("key_v1")
                .sizeBytes(100)
                .sha256Hash("hash1")
                .createdBy(owner)
                .build();

        versionDto = FileVersionResponseDto.builder()
                .id(version1.getId())
                .fileId(fileMetadata.getId())
                .versionNumber(1)
                .storageKey("key_v1")
                .sizeBytes(100)
                .build();
    }

    @Test
    void getVersionsForFile_Success() {
        when(fileMetadataRepository.findById(fileMetadata.getId())).thenReturn(Optional.of(fileMetadata));
        when(fileVersionRepository.findByFileMetadataIdOrderByVersionNumberDesc(fileMetadata.getId())).thenReturn(List.of(version1));
        when(fileVersionMapper.toDto(version1)).thenReturn(versionDto);

        List<FileVersionResponseDto> result = fileVersionService.getVersionsForFile(fileMetadata.getId(), ownerId);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getVersionNumber());
    }

    @Test
    void restoreFileVersion_Success() {
        when(fileMetadataRepository.findById(fileMetadata.getId())).thenReturn(Optional.of(fileMetadata));
        when(fileVersionRepository.findByFileMetadataIdAndVersionNumber(fileMetadata.getId(), 1)).thenReturn(Optional.of(version1));
        when(fileVersionRepository.save(any(FileVersion.class))).thenReturn(version1);
        when(fileVersionMapper.toDto(any())).thenReturn(versionDto);

        FileVersionResponseDto restored = fileVersionService.restoreFileVersion(fileMetadata.getId(), 1, ownerId);

        assertNotNull(restored);
        verify(fileMetadataRepository, times(1)).save(fileMetadata);
    }
}
