package com.fileshare.sharing.controller;

import com.fileshare.file.service.FileService;
import com.fileshare.sharing.dto.ShareResponseDto;
import com.fileshare.sharing.service.SharingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
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
    @Operation(summary = "Access shared file", description = "Public endpoint to inspect or download a file using its share token.")
    public ResponseEntity<?> accessSharedFile(
            @PathVariable("token") String token,
            @RequestParam(value = "download", defaultValue = "true") boolean download) {

        if (!download) {
            ShareResponseDto shareInfo = sharingService.getShareByToken(token);
            return ResponseEntity.ok(shareInfo);
        }

        FileService.FileDownloadResult result = sharingService.downloadSharedFile(token);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(result.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.filename() + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(result.sizeBytes()))
                .body(result.resource());
    }
}
