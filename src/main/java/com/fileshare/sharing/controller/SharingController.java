package com.fileshare.sharing.controller;

import com.fileshare.auth.security.UserPrincipal;
import com.fileshare.common.dto.ApiResponse;
import com.fileshare.sharing.dto.CreateShareRequest;
import com.fileshare.sharing.dto.ShareResponseDto;
import com.fileshare.sharing.service.SharingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files/{fileId}/share")
@Tag(name = "File Sharing", description = "Endpoints for creating, fetching, and revoking shareable public links")
@SecurityRequirement(name = "bearerAuth")
public class SharingController {

    private final SharingService sharingService;

    public SharingController(SharingService sharingService) {
        this.sharingService = sharingService;
    }

    @PostMapping
    @Operation(summary = "Generate shareable link", description = "Generates or returns a stable unique public token/link for a file with optional expiry and permissions.")
    public ResponseEntity<ApiResponse<ShareResponseDto>> createShare(
            @PathVariable("fileId") UUID fileId,
            @Valid @RequestBody CreateShareRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        ShareResponseDto response = sharingService.createShare(fileId, request, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Public share link generated successfully"));
    }

    @GetMapping
    @Operation(summary = "Get active shareable link", description = "Returns existing active share link for a file if present.")
    public ResponseEntity<ApiResponse<ShareResponseDto>> getShareForFile(
            @PathVariable("fileId") UUID fileId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        ShareResponseDto response = sharingService.getShareForFile(fileId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Active share link retrieved successfully"));
    }

    @DeleteMapping("/{shareId}")
    @Operation(summary = "Revoke shareable link", description = "Revokes an active share link so public access is denied.")
    public ResponseEntity<ApiResponse<Void>> revokeShare(
            @PathVariable("fileId") UUID fileId,
            @PathVariable("shareId") UUID shareId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        sharingService.revokeShare(fileId, shareId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Share link revoked successfully"));
    }
}
