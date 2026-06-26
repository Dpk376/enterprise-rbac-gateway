package com.enterprise.policy.gateway.admin;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface PolicyRepository extends ReactiveCrudRepository<Policy, String> {
}
