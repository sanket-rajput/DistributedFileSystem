package com.fileshare.audit.service;

import com.fileshare.audit.dto.AuditLogResponseDto;
import com.fileshare.audit.entity.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AuditLogService {

    Page<AuditLogResponseDto> getAuditLogs(UUID userId, AuditAction action, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);
}
