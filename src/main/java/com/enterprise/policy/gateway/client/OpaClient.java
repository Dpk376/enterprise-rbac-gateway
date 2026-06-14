package com.enterprise.policy.gateway.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class OpaClient {

    private final WebClient webClient;

    public OpaClient(@Value("${opa.url}") String opaUrl, WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(opaUrl).build();
    }

    public Mono<OpaResponse> checkAuthorization(Map<String, Object> input) {
        OpaRequest request = new OpaRequest(input);
        return webClient.post()
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OpaResponse.class)
                .onErrorResume(e -> {
                    // Fallback to deny on failure
                    return Mono.just(new OpaResponse(new OpaResult(false, "Error connecting to OPA: " + e.getMessage())));
                });
    }
}
