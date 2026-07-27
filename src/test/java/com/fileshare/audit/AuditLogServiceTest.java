package com.fileshare.audit;

import com.fileshare.audit.dto.AuditLogResponseDto;
import com.fileshare.audit.entity.AuditAction;
import com.fileshare.audit.entity.AuditLog;
import com.fileshare.audit.mapper.AuditLogMapper;
import com.fileshare.audit.repository.AuditLogRepository;
import com.fileshare.audit.service.AuditLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock private AuditLogRepository auditLogRepository;
    @Mock private AuditLogMapper auditLogMapper;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    private UUID userId;
    private AuditLog auditLog;
    private AuditLogResponseDto auditLogDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        auditLog = AuditLog.builder().id(UUID.randomUUID()).userId(userId).action(AuditAction.UPLOAD).details("Upload action").build();
        auditLogDto = AuditLogResponseDto.builder().id(auditLog.getId()).userId(userId).action(AuditAction.UPLOAD).details("Upload action").build();
    }

    @Test
    void getAuditLogs_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditLog> page = new PageImpl<>(List.of(auditLog));

        when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(auditLogMapper.toDto(auditLog)).thenReturn(auditLogDto);

        Page<AuditLogResponseDto> result = auditLogService.getAuditLogs(userId, AuditAction.UPLOAD, null, null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(AuditAction.UPLOAD, result.getContent().get(0).getAction());
    }
}
