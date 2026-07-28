package com.fileshare.sharing.controller;

import com.fileshare.common.dto.ApiResponse;
import com.fileshare.file.service.FileService;
import com.fileshare.sharing.dto.ShareResponseDto;
import com.fileshare.sharing.service.SharingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/share")
@Tag(name = "Public File Link Access", description = "Public endpoint for viewing/downloading shared files without authentication")
public class PublicShareController {

    private final SharingService sharingService;

    public PublicShareController(SharingService sharingService) {
        this.sharingService = sharingService;
    }

    @GetMapping("/{token}")
    @Operation(summary = "Access shared file metadata", description = "Public endpoint to inspect shared file metadata using its share token.")
    public ResponseEntity<ApiResponse<ShareResponseDto>> accessSharedFileMetadata(@PathVariable("token") String token) {
        ShareResponseDto shareInfo = sharingService.getShareByToken(token);
        return ResponseEntity.ok(ApiResponse.success(shareInfo, "Share metadata retrieved successfully"));
    }

    @GetMapping("/{token}/stream")
    @Operation(summary = "Stream shared file content", description = "Public endpoint to stream file content for inline viewing or download.")
    public ResponseEntity<?> streamSharedFileContent(
            @PathVariable("token") String token,
            @RequestParam(value = "inline", defaultValue = "true") boolean inline) {

        FileService.FileDownloadResult result = sharingService.streamSharedFile(token, inline);

        String disposition = inline ? "inline" : "attachment";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(result.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + result.filename() + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(result.sizeBytes()))
                .body(result.resource());
    }
}
