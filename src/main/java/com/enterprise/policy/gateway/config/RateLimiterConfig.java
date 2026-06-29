package com.enterprise.policy.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Configuration
public class RateLimiterConfig {

    /**
     * Resolves the rate limit key based on the authenticated principal's name.
     * If no user is authenticated, it defaults to a common "anonymous" key or the client's IP.
     */
    @Bean
    public KeyResolver principalNameKeyResolver() {
        return exchange -> ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .map(Principal::getName)
                .switchIfEmpty(Mono.just(exchange.getRequest().getRemoteAddress() != null 
                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() 
                        : "anonymous"));
    }
}
