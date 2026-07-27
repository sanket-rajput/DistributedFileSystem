package com.fileshare.audit.controller;

import com.fileshare.audit.dto.AuditLogResponseDto;
import com.fileshare.audit.entity.AuditAction;
import com.fileshare.audit.service.AuditLogService;
import com.fileshare.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit-logs")
@Tag(name = "Audit Logging", description = "Admin-only endpoints for reviewing system audit trail logs")
@SecurityRequirement(name = "bearerAuth")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get audit logs (Admin Only)", description = "Returns paginated and filterable audit log entries across system operations.")
    public ResponseEntity<ApiResponse<Page<AuditLogResponseDto>>> getAuditLogs(
            @RequestParam(value = "userId", required = false) UUID userId,
            @RequestParam(value = "action", required = false) AuditAction action,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditLogResponseDto> response = auditLogService.getAuditLogs(userId, action, fromDate, toDate, pageable);

        return ResponseEntity.ok(ApiResponse.success(response, "Audit logs retrieved successfully (Admin Access)"));
    }
}
