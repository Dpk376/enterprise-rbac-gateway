package com.enterprise.policy.gateway.route;

import io.r2dbc.postgresql.codec.Json;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("gateway_routes")
public class GatewayRoute implements Persistable<String> {

    @Id
    @Column("route_id")
    private String routeId;

    private String uri;
    private Json predicates;
    private Json filters;
    private Json metadata;

    @Column("route_order")
    private Integer routeOrder;

    private LocalDateTime createdAt;

    @Transient
    private boolean isNew = true;

    public GatewayRoute() {
    }

    public GatewayRoute(String routeId, String uri, Json predicates, Json filters, Json metadata, Integer routeOrder, LocalDateTime createdAt) {
        this.routeId = routeId;
        this.uri = uri;
        this.predicates = predicates;
        this.filters = filters;
        this.metadata = metadata;
        this.routeOrder = routeOrder;
        this.createdAt = createdAt;
    }

    @Override
    public String getId() {
        return routeId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    // Getters and Setters
    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Json getPredicates() {
        return predicates;
    }

    public void setPredicates(Json predicates) {
        this.predicates = predicates;
    }

    public Json getFilters() {
        return filters;
    }

    public void setFilters(Json filters) {
        this.filters = filters;
    }

    public Json getMetadata() {
        return metadata;
    }

    public void setMetadata(Json metadata) {
        this.metadata = metadata;
    }

    public Integer getRouteOrder() {
        return routeOrder;
    }

    public void setRouteOrder(Integer routeOrder) {
        this.routeOrder = routeOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
