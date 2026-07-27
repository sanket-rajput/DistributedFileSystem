package com.fileshare.folder.controller;

import com.fileshare.auth.security.UserPrincipal;
import com.fileshare.common.dto.ApiResponse;
import com.fileshare.folder.dto.CreateFolderRequest;
import com.fileshare.folder.dto.FolderResponseDto;
import com.fileshare.folder.dto.RenameFolderRequest;
import com.fileshare.folder.service.FolderService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/folders")
@Tag(name = "Folder Management", description = "Endpoints for folder creation, listing, renaming, and deletion")
@SecurityRequirement(name = "bearerAuth")
public class FolderController {

    private final FolderService folderService;

    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @PostMapping
    @Operation(summary = "Create a folder", description = "Creates a new root or nested folder for the authenticated user.")
    public ResponseEntity<ApiResponse<FolderResponseDto>> createFolder(
            @Valid @RequestBody CreateFolderRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        FolderResponseDto response = folderService.createFolder(request, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Folder created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get folder details", description = "Returns details of a folder by ID.")
    public ResponseEntity<ApiResponse<FolderResponseDto>> getFolderById(
            @PathVariable("id") UUID folderId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        FolderResponseDto response = folderService.getFolderById(folderId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Folder details retrieved successfully"));
    }

    @GetMapping
    @Operation(summary = "List folders", description = "Lists root folders or subfolders under parentId for authenticated user.")
    public ResponseEntity<ApiResponse<List<FolderResponseDto>>> listFolders(
            @RequestParam(value = "parentId", required = false) UUID parentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        List<FolderResponseDto> response = folderService.listFolders(parentId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Folders listed successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Rename a folder", description = "Renames an existing folder owned by the authenticated user.")
    public ResponseEntity<ApiResponse<FolderResponseDto>> renameFolder(
            @PathVariable("id") UUID folderId,
            @Valid @RequestBody RenameFolderRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        FolderResponseDto response = folderService.renameFolder(folderId, request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Folder renamed successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a folder", description = "Recursively deletes folder, subfolders, and all contained files in PostgreSQL and MinIO.")
    public ResponseEntity<ApiResponse<Void>> deleteFolder(
            @PathVariable("id") UUID folderId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        folderService.deleteFolder(folderId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Folder deleted successfully"));
    }
}
