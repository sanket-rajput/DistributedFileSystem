package com.fileshare.file.controller;

import com.fileshare.auth.security.UserPrincipal;
import com.fileshare.common.dto.ApiResponse;
import com.fileshare.file.dto.FileResponseDto;
import com.fileshare.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
@Tag(name = "File Management", description = "Endpoints for file upload, download, metadata, searching, and deletion")
@SecurityRequirement(name = "bearerAuth")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a file", description = "Streams a file to S3/MinIO while computing SHA-256 hash. Performs auto-deduplication and auto-versioning.")
    public ResponseEntity<ApiResponse<FileResponseDto>> uploadFile(
            @Parameter(description = "Multipart file to upload", required = true)
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "Optional parent folder ID")
            @RequestParam(value = "folderId", required = false) UUID folderId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        FileResponseDto response = fileService.uploadFile(file, folderId, userPrincipal.getId());
        String msg = response.isDeduplicated() ? "File uploaded (Deduplicated via SHA-256)" : "File uploaded successfully";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, msg));
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download a file", description = "Streams raw bytes from MinIO with original Content-Type and Content-Disposition headers.")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable("id") UUID fileId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        FileService.FileDownloadResult result = fileService.downloadFile(fileId, userPrincipal.getId());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(result.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.filename() + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(result.sizeBytes()))
                .body(result.resource());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get file metadata", description = "Returns cached metadata for a specific file by ID using Redis cache-aside.")
    public ResponseEntity<ApiResponse<FileResponseDto>> getFileMetadata(
            @PathVariable("id") UUID fileId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        FileResponseDto response = fileService.getFileMetadata(fileId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "File metadata retrieved successfully"));
    }

    @GetMapping
    @Operation(summary = "List files", description = "List files owned by user in root directory or under specified folder ID.")
    public ResponseEntity<ApiResponse<List<FileResponseDto>>> listFiles(
            @RequestParam(value = "folderId", required = false) UUID folderId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        List<FileResponseDto> response = fileService.listFiles(folderId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Files listed successfully"));
    }

    @GetMapping("/search")
    @Operation(summary = "Search files with combinable filters", description = "Dynamic JPA Specification search filterable by name, type, size range, folder, and date range.")
    public ResponseEntity<ApiResponse<Page<FileResponseDto>>> searchFiles(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "contentType", required = false) String contentType,
            @RequestParam(value = "folderId", required = false) UUID folderId,
            @RequestParam(value = "minSize", required = false) Long minSize,
            @RequestParam(value = "maxSize", required = false) Long maxSize,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<FileResponseDto> response = fileService.searchFiles(
                name, contentType, folderId, minSize, maxSize, fromDate, toDate, pageable, userPrincipal.getId()
        );

        return ResponseEntity.ok(ApiResponse.success(response, "Files search results retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a file", description = "Deletes file object from MinIO and metadata from PostgreSQL. Evicts Redis cache entry.")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @PathVariable("id") UUID fileId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        fileService.deleteFile(fileId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "File deleted successfully"));
    }
}
