package com.fileshare.folder;

import com.fileshare.common.exception.AccessDeniedException;
import com.fileshare.common.exception.DuplicateResourceException;
import com.fileshare.file.service.FileService;
import com.fileshare.folder.dto.CreateFolderRequest;
import com.fileshare.folder.dto.FolderResponseDto;
import com.fileshare.folder.dto.RenameFolderRequest;
import com.fileshare.folder.entity.Folder;
import com.fileshare.folder.mapper.FolderMapper;
import com.fileshare.folder.repository.FolderRepository;
import com.fileshare.folder.service.FolderServiceImpl;
import com.fileshare.user.entity.User;
import com.fileshare.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FolderServiceTest {

    @Mock
    private FolderRepository folderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FileService fileService;
    @Mock
    private FolderMapper folderMapper;

    @InjectMocks
    private FolderServiceImpl folderService;

    private User owner;
    private UUID ownerId;
    private Folder sampleFolder;
    private FolderResponseDto folderDto;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        owner = User.builder().id(ownerId).email("owner@example.com").build();
        sampleFolder = Folder.builder().id(UUID.randomUUID()).name("Documents").owner(owner).build();
        folderDto = FolderResponseDto.builder().id(sampleFolder.getId()).name("Documents").ownerId(ownerId).build();
    }

    @Test
    void createFolder_RootSuccess() {
        CreateFolderRequest req = new CreateFolderRequest("Documents", null);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(folderRepository.existsByNameAndOwnerIdAndParentFolderIsNull("Documents", ownerId)).thenReturn(false);
        when(folderRepository.save(any(Folder.class))).thenReturn(sampleFolder);
        when(folderMapper.toDto(sampleFolder)).thenReturn(folderDto);

        FolderResponseDto res = folderService.createFolder(req, ownerId);

        assertNotNull(res);
        assertEquals("Documents", res.getName());
    }

    @Test
    void createFolder_DuplicateName_ThrowsException() {
        CreateFolderRequest req = new CreateFolderRequest("Documents", null);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(folderRepository.existsByNameAndOwnerIdAndParentFolderIsNull("Documents", ownerId)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> folderService.createFolder(req, ownerId));
    }

    @Test
    void getFolderById_OwnershipDenied_ThrowsException() {
        UUID otherUserId = UUID.randomUUID();
        when(folderRepository.findById(sampleFolder.getId())).thenReturn(Optional.of(sampleFolder));

        assertThrows(AccessDeniedException.class, () -> folderService.getFolderById(sampleFolder.getId(), otherUserId));
    }

    @Test
    void renameFolder_Success() {
        RenameFolderRequest req = new RenameFolderRequest("NewName");
        when(folderRepository.findById(sampleFolder.getId())).thenReturn(Optional.of(sampleFolder));
        when(folderRepository.existsByNameAndOwnerIdAndParentFolderIsNull("NewName", ownerId)).thenReturn(false);
        when(folderRepository.save(any(Folder.class))).thenReturn(sampleFolder);
        when(folderMapper.toDto(any())).thenReturn(FolderResponseDto.builder().id(sampleFolder.getId()).name("NewName").build());

        FolderResponseDto result = folderService.renameFolder(sampleFolder.getId(), req, ownerId);

        assertEquals("NewName", result.getName());
    }
}
