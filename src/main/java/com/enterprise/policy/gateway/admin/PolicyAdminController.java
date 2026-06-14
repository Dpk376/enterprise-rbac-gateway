package com.enterprise.policy.gateway.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/admin/policies")
public class PolicyAdminController {

    private final Map<String, String> policies = new ConcurrentHashMap<>();

    @PostMapping("/{policyId}")
    public Mono<ResponseEntity<String>> uploadPolicy(@PathVariable String policyId, @RequestBody String regoPolicy) {
        policies.put(policyId, regoPolicy);
        // This is a placeholder; in a real scenario, we push to OPA's REST API:
        // PUT /v1/policies/{policyId}
        return Mono.just(ResponseEntity.ok("Policy " + policyId + " uploaded successfully"));
    }

    @GetMapping
    public Mono<ResponseEntity<Map<String, String>>> listPolicies() {
        return Mono.just(ResponseEntity.ok(policies));
    }
}
