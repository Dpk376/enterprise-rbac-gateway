package com.enterprise.policy.gateway.admin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/admin/policies")
public class PolicyAdminController {

    private final Map<String, String> policies = new ConcurrentHashMap<>();
    private final WebClient webClient;

    public PolicyAdminController(WebClient.Builder webClientBuilder, @Value("${opa.base-url}") String opaBaseUrl) {
        this.webClient = webClientBuilder.baseUrl(opaBaseUrl).build();
    }

    @PostMapping("/{policyId}")
    public Mono<ResponseEntity<String>> uploadPolicy(@PathVariable String policyId, @RequestBody String regoPolicy) {
        return webClient.put()
                .uri("/v1/policies/{policyId}", policyId)
                .header("Content-Type", "text/plain")
                .bodyValue(regoPolicy)
                .retrieve()
                .toBodilessEntity()
                .map(response -> {
                    policies.put(policyId, regoPolicy);
                    return ResponseEntity.ok("Policy " + policyId + " uploaded successfully");
                })
                .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().body("Failed to upload policy: " + e.getMessage())));
    }

    @GetMapping
    public Mono<ResponseEntity<Map<String, String>>> listPolicies() {
        return Mono.just(ResponseEntity.ok(policies));
    }
}
