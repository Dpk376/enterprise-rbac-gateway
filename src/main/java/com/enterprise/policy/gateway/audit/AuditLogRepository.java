package com.enterprise.policy.gateway.audit;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogRepository extends ReactiveCrudRepository<AuditLogEntry, UUID> {
}
