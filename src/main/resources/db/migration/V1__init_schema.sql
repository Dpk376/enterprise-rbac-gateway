CREATE TABLE authz_audit_log (
    id UUID PRIMARY KEY,
    transaction_id VARCHAR(255) NOT NULL,
    actor VARCHAR(255) NOT NULL,
    resource VARCHAR(1024) NOT NULL,
    method VARCHAR(10) NOT NULL,
    decision VARCHAR(10) NOT NULL,
    reason VARCHAR(1024),
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_audit_actor ON authz_audit_log(actor);
CREATE INDEX idx_audit_resource ON authz_audit_log(resource);
CREATE INDEX idx_audit_created_at ON authz_audit_log(created_at);
