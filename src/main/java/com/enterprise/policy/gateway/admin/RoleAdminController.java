package com.enterprise.policy.gateway.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/admin/roles")
public class RoleAdminController {

    private final Map<UUID, Map<String, Object>> roles = new ConcurrentHashMap<>();

    @PostMapping
    public Mono<ResponseEntity<Map<String, Object>>> createRole(@RequestBody Map<String, Object> roleDef) {
        UUID roleId = UUID.randomUUID();
        roleDef.put("id", roleId);
        roles.put(roleId, roleDef);
        return Mono.just(ResponseEntity.ok(roleDef));
    }

    @GetMapping
    public Mono<ResponseEntity<Map<UUID, Map<String, Object>>>> listRoles() {
        return Mono.just(ResponseEntity.ok(roles));
    }

    @DeleteMapping("/{roleId}")
    public Mono<ResponseEntity<Void>> deleteRole(@PathVariable UUID roleId) {
        roles.remove(roleId);
        return Mono.just(ResponseEntity.noContent().build());
    }
}
