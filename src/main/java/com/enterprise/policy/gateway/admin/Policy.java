package com.enterprise.policy.gateway.admin;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("policies")
public record Policy(
    @Id String id,
    String content,
    LocalDateTime updatedAt
) {}
