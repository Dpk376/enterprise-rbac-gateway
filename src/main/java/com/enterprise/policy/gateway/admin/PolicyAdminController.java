package com.enterprise.policy.gateway.admin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/policies")
public class PolicyAdminController {

    private final PolicyRepository policyRepository;
    private final WebClient webClient;

    public PolicyAdminController(PolicyRepository policyRepository, WebClient.Builder webClientBuilder, @Value("${opa.base-url}") String opaBaseUrl) {
        this.policyRepository = policyRepository;
        this.webClient = webClientBuilder.baseUrl(opaBaseUrl).build();
    }

    @PostMapping("/{policyId}")
    public Mono<ResponseEntity<String>> uploadPolicy(@PathVariable String policyId, @RequestBody String regoPolicy) {
        Policy policy = new Policy(policyId, regoPolicy, LocalDateTime.now());
        
        return policyRepository.save(policy)
                .flatMap(saved -> webClient.put()
                        .uri("/v1/policies/{policyId}", policyId)
                        .header("Content-Type", "text/plain")
                        .bodyValue(regoPolicy)
                        .retrieve()
                        .toBodilessEntity()
                )
                .map(response -> ResponseEntity.ok("Policy " + policyId + " uploaded successfully"))
                .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().body("Failed to upload policy: " + e.getMessage())));
    }

    @GetMapping
    public Flux<Policy> listPolicies() {
        return policyRepository.findAll();
    }
}
