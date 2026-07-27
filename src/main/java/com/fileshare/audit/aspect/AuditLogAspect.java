package com.fileshare.audit.aspect;

import com.fileshare.audit.annotation.Auditable;
import com.fileshare.audit.entity.AuditLog;
import com.fileshare.audit.repository.AuditLogRepository;
import com.fileshare.auth.security.UserPrincipal;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
public class AuditLogAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditLogAspect.class);

    private final AuditLogRepository auditLogRepository;

    public AuditLogAspect(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void auditMethodExecution(JoinPoint joinPoint, Auditable auditable, Object result) {
        try {
            UUID userId = null;
            String userEmail = "ANONYMOUS";

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
                userId = principal.getId();
                userEmail = principal.getEmail();
            }

            String details = "Method " + joinPoint.getSignature().getName() + " executed successfully.";

            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .userEmail(userEmail)
                    .action(auditable.action())
                    .resourceType(auditable.resourceType())
                    .details(details)
                    .build();

            auditLogRepository.save(auditLog);
            log.info("AOP AuditLog saved: Action={} User={}", auditable.action(), userEmail);
        } catch (Exception e) {
            log.error("Failed to persist AOP audit log: {}", e.getMessage());
        }
    }
}
