package com.fileshare.sharing.service;

import com.fileshare.file.service.FileService;
import com.fileshare.sharing.dto.CreateShareRequest;
import com.fileshare.sharing.dto.ShareResponseDto;

import java.util.UUID;

public interface SharingService {

    ShareResponseDto createShare(UUID fileId, CreateShareRequest request, UUID ownerId);

    ShareResponseDto getShareByToken(String token);

    FileService.FileDownloadResult downloadSharedFile(String token);

    void revokeShare(UUID fileId, UUID shareId, UUID ownerId);
}
