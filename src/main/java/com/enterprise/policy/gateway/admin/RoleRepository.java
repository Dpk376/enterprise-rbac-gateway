package com.enterprise.policy.gateway.admin;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import java.util.UUID;

public interface RoleRepository extends ReactiveCrudRepository<Role, UUID> {
}
