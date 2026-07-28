package com.fileshare.sharing.service;

import com.fileshare.file.service.FileService;
import com.fileshare.sharing.dto.CreateShareRequest;
import com.fileshare.sharing.dto.ShareResponseDto;

import java.util.UUID;

public interface SharingService {

    ShareResponseDto createShare(UUID fileId, CreateShareRequest request, UUID ownerId);

    ShareResponseDto getShareByToken(String token);

    ShareResponseDto getShareForFile(UUID fileId, UUID ownerId);

    FileService.FileDownloadResult downloadSharedFile(String token);

    FileService.FileDownloadResult streamSharedFile(String token, boolean inline);

    void revokeShare(UUID fileId, UUID shareId, UUID ownerId);
}
