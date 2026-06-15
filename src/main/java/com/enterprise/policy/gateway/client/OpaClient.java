package com.enterprise.policy.gateway.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class OpaClient {

    private final WebClient webClient;
    private final ReactiveCircuitBreaker circuitBreaker;

    public OpaClient(@Value("${opa.url}") String opaUrl, 
                     WebClient.Builder webClientBuilder,
                     ReactiveCircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.webClient = webClientBuilder.baseUrl(opaUrl).build();
        this.circuitBreaker = circuitBreakerFactory.create("opaClient");
    }

    public Mono<OpaResponse> checkAuthorization(Map<String, Object> input) {
        OpaRequest request = new OpaRequest(input);
        Mono<OpaResponse> call = webClient.post()
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OpaResponse.class);

        return circuitBreaker.run(call, throwable -> {
            // Fallback to deny when OPA fails or circuit is open
            return Mono.just(new OpaResponse(new OpaResult(false, "Circuit Open / OPA Unavailable: " + throwable.getMessage())));
        });
    }
}
