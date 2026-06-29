package com.enterprise.policy.gateway.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import reactor.util.retry.Retry;
import java.time.Duration;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);
    // Dedicated logger for Dead Letter Queue to allow easy scraping/reprocessing
    private static final Logger dlqLog = LoggerFactory.getLogger("DLQ_AUDIT");

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Asynchronously records an authorization decision without blocking the caller.
     * Includes retries and a DLQ fallback to ensure zero data loss.
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
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .doBeforeRetry(retrySignal -> log.warn("Retrying audit log save for transaction: {}, attempt: {}", transactionId, retrySignal.totalRetries())))
                .doOnSuccess(saved -> log.debug("Audit log saved successfully for transaction: {}", transactionId))
                .onErrorResume(error -> {
                    log.error("Exhausted retries. Failing over to DLQ for transaction: {}", transactionId, error);
                    dlqLog.error("DLQ_AUDIT: transactionId={}, actor={}, resource={}, method={}, decision={}, reason={}", 
                            transactionId, actor, resource, method, decision, reason);
                    return Mono.empty();
                })
                .subscribe(); // Subscribe on a separate context to make it fire-and-forget
    }
}
