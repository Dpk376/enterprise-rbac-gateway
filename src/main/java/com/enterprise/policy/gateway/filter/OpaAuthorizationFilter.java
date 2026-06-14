package com.enterprise.policy.gateway.filter;

import com.enterprise.policy.gateway.audit.AuditLogService;
import com.enterprise.policy.gateway.client.OpaClient;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OpaAuthorizationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(OpaAuthorizationFilter.class);

    private final OpaClient opaClient;
    private final AuditLogService auditLogService;
    private final MeterRegistry meterRegistry;

    public OpaAuthorizationFilter(OpaClient opaClient, AuditLogService auditLogService, MeterRegistry meterRegistry) {
        this.opaClient = opaClient;
        this.auditLogService = auditLogService;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (!path.startsWith("/api/")) {
            return chain.filter(exchange);
        }

        String transactionId = exchange.getRequest().getId();
        String method = exchange.getRequest().getMethod().name();

        return ReactiveSecurityContextHolder.getContext()
            .map(securityContext -> securityContext.getAuthentication())
            .cast(JwtAuthenticationToken.class)
            .flatMap(jwtAuth -> {
                String actor = jwtAuth.getName();
                Map<String, Object> claims = jwtAuth.getToken().getClaims();

                Map<String, Object> input = new HashMap<>();
                input.put("method", method);
                input.put("path", List.of(path.split("/")));
                input.put("claims", claims);

                long start = System.nanoTime();

                return opaClient.checkAuthorization(input)
                    .doFinally(signalType -> {
                        long duration = System.nanoTime() - start;
                        meterRegistry.timer("gateway.authz.opa.latency").record(duration, java.util.concurrent.TimeUnit.NANOSECONDS);
                    })
                    .flatMap(opaResponse -> {
                        boolean allowed = opaResponse != null && opaResponse.result() != null && opaResponse.result().allow();
                        String reason = (opaResponse != null && opaResponse.result() != null && opaResponse.result().reason() != null) 
                            ? opaResponse.result().reason() : (allowed ? "Authorized" : "Denied by policy");

                        meterRegistry.counter("gateway.authz.decisions", "decision", allowed ? "allow" : "deny", "route", path).increment();
                        auditLogService.recordDecision(transactionId, actor, path, method, allowed ? "ALLOW" : "DENY", reason);

                        if (allowed) {
                            return chain.filter(exchange);
                        } else {
                            log.warn("Authorization denied for user: {} on path: {}", actor, path);
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            return exchange.getResponse().setComplete();
                        }
                    });
            })
            .switchIfEmpty(Mono.defer(() -> {
                log.warn("No authentication found for path: {}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
