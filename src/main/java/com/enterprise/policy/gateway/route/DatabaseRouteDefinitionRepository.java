package com.enterprise.policy.gateway.route;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class DatabaseRouteDefinitionRepository implements RouteDefinitionRepository {

    private final GatewayRouteRepository repository;
    private final ObjectMapper objectMapper;

    public DatabaseRouteDefinitionRepository(GatewayRouteRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return repository.findAll().map(this::convertToRouteDefinition);
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return route.flatMap(routeDefinition -> {
            GatewayRoute gatewayRoute = convertToGatewayRoute(routeDefinition);
            return repository.findById(gatewayRoute.getRouteId())
                    .flatMap(existing -> {
                        gatewayRoute.setNew(false);
                        gatewayRoute.setCreatedAt(existing.getCreatedAt());
                        return repository.save(gatewayRoute);
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        gatewayRoute.setNew(true);
                        return repository.save(gatewayRoute);
                    }));
        }).then();
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.flatMap(repository::deleteById);
    }

    private RouteDefinition convertToRouteDefinition(GatewayRoute gatewayRoute) {
        RouteDefinition routeDefinition = new RouteDefinition();
        routeDefinition.setId(gatewayRoute.getRouteId());
        routeDefinition.setUri(URI.create(gatewayRoute.getUri()));
        routeDefinition.setOrder(gatewayRoute.getRouteOrder() != null ? gatewayRoute.getRouteOrder() : 0);

        try {
            if (gatewayRoute.getPredicates() != null) {
                List<PredicateDefinition> predicates = objectMapper.readValue(gatewayRoute.getPredicates().asString(), new TypeReference<List<PredicateDefinition>>() {});
                routeDefinition.setPredicates(predicates);
            }
            if (gatewayRoute.getFilters() != null) {
                List<FilterDefinition> filters = objectMapper.readValue(gatewayRoute.getFilters().asString(), new TypeReference<List<FilterDefinition>>() {});
                routeDefinition.setFilters(filters);
            }
            if (gatewayRoute.getMetadata() != null) {
                Map<String, Object> metadata = objectMapper.readValue(gatewayRoute.getMetadata().asString(), new TypeReference<Map<String, Object>>() {});
                routeDefinition.setMetadata(metadata);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error deserializing route definition", e);
        }

        return routeDefinition;
    }

    private GatewayRoute convertToGatewayRoute(RouteDefinition routeDefinition) {
        GatewayRoute gatewayRoute = new GatewayRoute();
        gatewayRoute.setRouteId(routeDefinition.getId());
        gatewayRoute.setUri(routeDefinition.getUri().toString());
        gatewayRoute.setRouteOrder(routeDefinition.getOrder());
        gatewayRoute.setCreatedAt(LocalDateTime.now());

        try {
            gatewayRoute.setPredicates(Json.of(objectMapper.writeValueAsString(routeDefinition.getPredicates())));
            gatewayRoute.setFilters(Json.of(objectMapper.writeValueAsString(routeDefinition.getFilters())));
            gatewayRoute.setMetadata(Json.of(objectMapper.writeValueAsString(routeDefinition.getMetadata())));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing route definition", e);
        }

        return gatewayRoute;
    }
}
