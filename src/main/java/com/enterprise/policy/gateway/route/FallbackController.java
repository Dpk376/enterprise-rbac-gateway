package com.enterprise.policy.gateway.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
public class FallbackController {

    private static final Logger log = LoggerFactory.getLogger(FallbackController.class);

    @RequestMapping("/fallback")
    public Mono<Void> fallback() {
        log.warn("Circuit breaker triggered, executing fallback.");
        return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Downstream service is currently unavailable. Please try again later."));
    }
}
