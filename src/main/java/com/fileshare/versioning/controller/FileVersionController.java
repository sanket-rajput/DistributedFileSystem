package com.fileshare.versioning.controller;

import com.fileshare.auth.security.UserPrincipal;
import com.fileshare.common.dto.ApiResponse;
import com.fileshare.file.service.FileService;
import com.fileshare.versioning.dto.FileVersionResponseDto;
import com.fileshare.versioning.service.FileVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files/{fileId}/versions")
@Tag(name = "File Versioning", description = "Endpoints for listing, downloading, and restoring file versions")
@SecurityRequirement(name = "bearerAuth")
public class FileVersionController {

    private final FileVersionService fileVersionService;

    public FileVersionController(FileVersionService fileVersionService) {
        this.fileVersionService = fileVersionService;
    }

    @GetMapping
    @Operation(summary = "List file versions", description = "Returns version history for a specified file.")
    public ResponseEntity<ApiResponse<List<FileVersionResponseDto>>> getVersionsForFile(
            @PathVariable("fileId") UUID fileId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        List<FileVersionResponseDto> versions = fileVersionService.getVersionsForFile(fileId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(versions, "File versions retrieved successfully"));
    }

    @GetMapping("/{versionNumber}/download")
    @Operation(summary = "Download specific file version", description = "Streams raw bytes of a historical version.")
    public ResponseEntity<Resource> downloadFileVersion(
            @PathVariable("fileId") UUID fileId,
            @PathVariable("versionNumber") int versionNumber,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        FileService.FileDownloadResult result = fileVersionService.downloadFileVersion(fileId, versionNumber, userPrincipal.getId());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(result.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.filename() + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(result.sizeBytes()))
                .body(result.resource());
    }

    @PostMapping("/{versionNumber}/restore")
    @Operation(summary = "Restore file version", description = "Restores a historical version to become the active current version.")
    public ResponseEntity<ApiResponse<FileVersionResponseDto>> restoreFileVersion(
            @PathVariable("fileId") UUID fileId,
            @PathVariable("versionNumber") int versionNumber,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        FileVersionResponseDto response = fileVersionService.restoreFileVersion(fileId, versionNumber, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "File version restored successfully"));
    }
}
