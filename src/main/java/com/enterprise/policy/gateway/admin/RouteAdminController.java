package com.enterprise.policy.gateway.admin;

import com.enterprise.policy.gateway.route.DatabaseRouteDefinitionRepository;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/admin/routes")
public class RouteAdminController {

    private final DatabaseRouteDefinitionRepository routeDefinitionLocator;
    private final ApplicationEventPublisher publisher;

    public RouteAdminController(DatabaseRouteDefinitionRepository routeDefinitionLocator, ApplicationEventPublisher publisher) {
        this.routeDefinitionLocator = routeDefinitionLocator;
        this.publisher = publisher;
    }

    @GetMapping
    public Flux<RouteDefinition> listRoutes() {
        return routeDefinitionLocator.getRouteDefinitions();
    }

    @PostMapping
    public Mono<ResponseEntity<String>> addRoute(@RequestBody RouteDefinition routeDefinition) {
        return routeDefinitionLocator.save(Mono.just(routeDefinition))
                .then(Mono.defer(() -> {
                    publisher.publishEvent(new RefreshRoutesEvent(this));
                    return Mono.just(ResponseEntity.ok("Route saved successfully"));
                }))
                .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().body("Failed to save route: " + e.getMessage())));
    }

    @DeleteMapping("/{routeId}")
    public Mono<ResponseEntity<String>> deleteRoute(@PathVariable String routeId) {
        return routeDefinitionLocator.delete(Mono.just(routeId))
                .then(Mono.defer(() -> {
                    publisher.publishEvent(new RefreshRoutesEvent(this));
                    return Mono.just(ResponseEntity.ok("Route deleted successfully"));
                }))
                .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().body("Failed to delete route: " + e.getMessage())));
    }
}
