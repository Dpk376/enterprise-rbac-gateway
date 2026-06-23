CREATE TABLE gateway_routes (
    route_id VARCHAR(255) PRIMARY KEY,
    uri VARCHAR(1024) NOT NULL,
    predicates JSONB NOT NULL,
    filters JSONB NOT NULL,
    metadata JSONB NOT NULL,
    route_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
