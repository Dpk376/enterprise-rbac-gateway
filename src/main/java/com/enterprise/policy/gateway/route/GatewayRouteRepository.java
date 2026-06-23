package com.enterprise.policy.gateway.route;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GatewayRouteRepository extends ReactiveCrudRepository<GatewayRoute, String> {
}
