package com.fileshare.audit.mapper;

import com.fileshare.audit.dto.AuditLogResponseDto;
import com.fileshare.audit.entity.AuditLog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    AuditLogResponseDto toDto(AuditLog auditLog);
}
