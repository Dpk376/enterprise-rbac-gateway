package com.enterprise.policy.gateway.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/roles")
public class RoleAdminController {

    private final RoleRepository roleRepository;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public RoleAdminController(RoleRepository roleRepository, WebClient.Builder webClientBuilder, 
                               @Value("${opa.base-url}") String opaBaseUrl, ObjectMapper objectMapper) {
        this.roleRepository = roleRepository;
        this.webClient = webClientBuilder.baseUrl(opaBaseUrl).build();
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public Mono<ResponseEntity<Role>> createRole(@RequestBody Map<String, Object> roleDef) {
        UUID roleId = UUID.randomUUID();
        String name = (String) roleDef.get("name");
        String description = (String) roleDef.get("description");
        
        String permissionsJson;
        try {
            permissionsJson = objectMapper.writeValueAsString(roleDef.get("permissions"));
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Invalid permissions format", e));
        }

        Role newRole = new Role(roleId, name, description, Json.of(permissionsJson), LocalDateTime.now());
        
        return roleRepository.save(newRole)
                .flatMap(savedRole -> pushRolesToOpa().thenReturn(ResponseEntity.ok(savedRole)));
    }

    @GetMapping
    public Mono<ResponseEntity<Map<UUID, Role>>> listRoles() {
        return roleRepository.findAll()
                .collectMap(Role::id, role -> role)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{roleId}")
    public Mono<ResponseEntity<Void>> deleteRole(@PathVariable UUID roleId) {
        return roleRepository.deleteById(roleId)
                .then(pushRolesToOpa())
                .thenReturn(ResponseEntity.noContent().build());
    }
    
    private Mono<Void> pushRolesToOpa() {
        return roleRepository.findAll()
            .collectList()
            .flatMap(roles -> {
                Map<String, Object> rolesData = roles.stream().collect(Collectors.toMap(
                    Role::name,
                    role -> {
                        try {
                            return objectMapper.readValue(role.permissions().asString(), Object.class);
                        } catch (JsonProcessingException e) {
                            return Map.of();
                        }
                    }
                ));
                
                return webClient.put()
                    .uri("/v1/data/roles")
                    .bodyValue(rolesData)
                    .retrieve()
                    .bodyToMono(Void.class);
            });
    }
}
