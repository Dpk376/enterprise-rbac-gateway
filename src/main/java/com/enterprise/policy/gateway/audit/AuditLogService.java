package com.enterprise.policy.gateway.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Asynchronously records an authorization decision without blocking the caller.
     */
    public void recordDecision(String transactionId, String actor, String resource, String method, String decision, String reason) {
        AuditLogEntry entry = new AuditLogEntry(
                UUID.randomUUID(),
                transactionId,
                actor,
                resource,
                method,
                decision,
                reason,
                LocalDateTime.now(ZoneOffset.UTC)
        );

        auditLogRepository.save(entry)
                .doOnSuccess(saved -> log.debug("Audit log saved successfully for transaction: {}", transactionId))
                .doOnError(error -> log.error("Failed to save audit log for transaction: {}", transactionId, error))
                .subscribe(); // Subscribe on a separate context to make it fire-and-forget
    }
}
