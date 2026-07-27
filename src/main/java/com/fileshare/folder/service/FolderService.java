package com.fileshare.folder.service;

import com.fileshare.folder.dto.CreateFolderRequest;
import com.fileshare.folder.dto.FolderResponseDto;
import com.fileshare.folder.dto.RenameFolderRequest;

import java.util.List;
import java.util.UUID;

public interface FolderService {

    FolderResponseDto createFolder(CreateFolderRequest request, UUID ownerId);

    FolderResponseDto getFolderById(UUID folderId, UUID ownerId);

    List<FolderResponseDto> listFolders(UUID parentFolderId, UUID ownerId);

    FolderResponseDto renameFolder(UUID folderId, RenameFolderRequest request, UUID ownerId);

    void deleteFolder(UUID folderId, UUID ownerId);
}
