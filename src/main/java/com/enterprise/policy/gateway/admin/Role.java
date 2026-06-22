package com.enterprise.policy.gateway.admin;

import io.r2dbc.postgresql.codec.Json;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("roles")
public record Role(
    @Id UUID id,
    String name,
    String description,
    Json permissions,
    LocalDateTime createdAt
) {}
