package com.fileshare.folder.service;

import com.fileshare.common.exception.AccessDeniedException;
import com.fileshare.common.exception.DuplicateResourceException;
import com.fileshare.common.exception.ResourceNotFoundException;
import com.fileshare.file.service.FileService;
import com.fileshare.folder.dto.CreateFolderRequest;
import com.fileshare.folder.dto.FolderResponseDto;
import com.fileshare.folder.dto.RenameFolderRequest;
import com.fileshare.folder.entity.Folder;
import com.fileshare.folder.mapper.FolderMapper;
import com.fileshare.folder.repository.FolderRepository;
import com.fileshare.user.entity.User;
import com.fileshare.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FolderServiceImpl implements FolderService {

    private static final Logger log = LoggerFactory.getLogger(FolderServiceImpl.class);

    private final FolderRepository folderRepository;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final FolderMapper folderMapper;

    public FolderServiceImpl(FolderRepository folderRepository,
                             UserRepository userRepository,
                             FileService fileService,
                             FolderMapper folderMapper) {
        this.folderRepository = folderRepository;
        this.userRepository = userRepository;
        this.fileService = fileService;
        this.folderMapper = folderMapper;
    }

    @Override
    @Transactional
    public FolderResponseDto createFolder(CreateFolderRequest request, UUID ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", ownerId));

        Folder parentFolder = null;
        if (request.getParentFolderId() != null) {
            parentFolder = folderRepository.findById(request.getParentFolderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Folder", "id", request.getParentFolderId()));

            if (!parentFolder.getOwner().getId().equals(ownerId)) {
                throw new AccessDeniedException("You do not have permission to access parent folder");
            }

            if (folderRepository.existsByNameAndOwnerIdAndParentFolderId(request.getName(), ownerId, request.getParentFolderId())) {
                throw new DuplicateResourceException("A folder named '" + request.getName() + "' already exists in this directory");
            }
        } else {
            if (folderRepository.existsByNameAndOwnerIdAndParentFolderIsNull(request.getName(), ownerId)) {
                throw new DuplicateResourceException("A root folder named '" + request.getName() + "' already exists");
            }
        }

        Folder folder = Folder.builder()
                .name(request.getName())
                .owner(owner)
                .parentFolder(parentFolder)
                .build();

        Folder savedFolder = folderRepository.save(folder);
        return folderMapper.toDto(savedFolder);
    }

    @Override
    @Transactional(readOnly = true)
    public FolderResponseDto getFolderById(UUID folderId, UUID ownerId) {
        Folder folder = getFolderEntityAndCheckOwner(folderId, ownerId);
        return folderMapper.toDto(folder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FolderResponseDto> listFolders(UUID parentFolderId, UUID ownerId) {
        List<Folder> folders;
        if (parentFolderId != null) {
            Folder parent = getFolderEntityAndCheckOwner(parentFolderId, ownerId);
            folders = folderRepository.findByOwnerIdAndParentFolderId(ownerId, parent.getId());
        } else {
            folders = folderRepository.findByOwnerIdAndParentFolderIsNull(ownerId);
        }

        return folders.stream()
                .map(folderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FolderResponseDto renameFolder(UUID folderId, RenameFolderRequest request, UUID ownerId) {
        Folder folder = getFolderEntityAndCheckOwner(folderId, ownerId);

        UUID parentId = folder.getParentFolder() != null ? folder.getParentFolder().getId() : null;
        if (parentId != null) {
            if (folderRepository.existsByNameAndOwnerIdAndParentFolderId(request.getName(), ownerId, parentId)) {
                throw new DuplicateResourceException("A folder named '" + request.getName() + "' already exists in this directory");
            }
        } else {
            if (folderRepository.existsByNameAndOwnerIdAndParentFolderIsNull(request.getName(), ownerId)) {
                throw new DuplicateResourceException("A root folder named '" + request.getName() + "' already exists");
            }
        }

        folder.setName(request.getName());
        Folder updatedFolder = folderRepository.save(folder);
        return folderMapper.toDto(updatedFolder);
    }

    @Override
    @Transactional
    public void deleteFolder(UUID folderId, UUID ownerId) {
        Folder folder = getFolderEntityAndCheckOwner(folderId, ownerId);
        deleteFolderRecursively(folder, ownerId);
    }

    private void deleteFolderRecursively(Folder folder, UUID ownerId) {
        List<Folder> subfolders = folderRepository.findByOwnerIdAndParentFolderId(ownerId, folder.getId());
        for (Folder subfolder : subfolders) {
            deleteFolderRecursively(subfolder, ownerId);
        }

        fileService.deleteFilesByFolder(folder.getId());
        folderRepository.delete(folder);
    }

    private Folder getFolderEntityAndCheckOwner(UUID folderId, UUID ownerId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", "id", folderId));

        if (!folder.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("You do not have permission to access this folder");
        }

        return folder;
    }
}
