package com.enterprise.policy.gateway.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class OpaPolicySynchronizer {

    private static final Logger log = LoggerFactory.getLogger(OpaPolicySynchronizer.class);

    private final PolicyRepository policyRepository;
    private final WebClient webClient;

    public OpaPolicySynchronizer(PolicyRepository policyRepository, WebClient.Builder webClientBuilder, @Value("${opa.base-url}") String opaBaseUrl) {
        this.policyRepository = policyRepository;
        this.webClient = webClientBuilder.baseUrl(opaBaseUrl).build();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void synchronizePolicies() {
        log.info("Starting OPA Policy Synchronization...");
        policyRepository.findAll()
                .flatMap(policy -> {
                    log.info("Pushing policy to OPA: {}", policy.id());
                    return webClient.put()
                            .uri("/v1/policies/{policyId}", policy.id())
                            .header("Content-Type", "text/plain")
                            .bodyValue(policy.content())
                            .retrieve()
                            .toBodilessEntity()
                            .onErrorResume(e -> {
                                log.error("Failed to push policy: {} - {}", policy.id(), e.getMessage());
                                return Mono.empty();
                            });
                })
                .doOnComplete(() -> log.info("OPA Policy Synchronization completed."))
                .subscribe();
    }
}
