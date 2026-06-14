package com.enterprise.policy.gateway.audit;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("authz_audit_log")
public class AuditLogEntry {

    @Id
    private UUID id;
    private String transactionId;
    private String actor;
    private String resource;
    private String method;
    private String decision;
    private String reason;
    private LocalDateTime createdAt;

    public AuditLogEntry() {}

    public AuditLogEntry(UUID id, String transactionId, String actor, String resource, String method, String decision, String reason, LocalDateTime createdAt) {
        this.id = id;
        this.transactionId = transactionId;
        this.actor = actor;
        this.resource = resource;
        this.method = method;
        this.decision = decision;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }

    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
