package com.tirmizee.service;

import com.tirmizee.property.ExternalApiProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BusinessService {

    private final WebClient externalApiClient;
    private final ExternalApiProperty externalApiProperty;

    public Mono<String> getProduct() {
        return externalApiClient.get()
                .uri(externalApiProperty.getProductApi().getGetProductPath())
                .retrieve()
                .bodyToMono(String.class);
    }

}
