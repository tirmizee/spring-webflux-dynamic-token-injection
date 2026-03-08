package com.tirmizee.config;

import com.tirmizee.model.TokenResponse;
import com.tirmizee.property.ExternalApiProperty;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2TokenFilter {

    private final ExternalApiProperty properties;
    private final WebClient.Builder webClientBuilder;
    private final AtomicReference<Mono<String>> tokenCache = new AtomicReference<>();

    private WebClient authApiClient;

    @PostConstruct
    public void init() {
        this.authApiClient = webClientBuilder
                .baseUrl(properties.getAuthProvider().getBaseUrl())
                .defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .defaultHeader(ACCEPT, APPLICATION_JSON_VALUE)
                .build();
        this.tokenCache.set(createTokenMono());
        log.info("OAuth2TokenFilter initialized with reactive token cache.");
    }

    private Mono<String> createTokenMono() {
        return Mono.defer(this::fetchToken)
                .cache(res -> {
                            int seconds = res.getData().getExpiresIn();
                            return Duration.ofSeconds(Math.max(0, seconds - 60));
                        },
                        ex -> Duration.ZERO,
                        () -> Duration.ZERO
                )
                .map(res -> res.getData().getAccessToken());
    }

    private Mono<TokenResponse> fetchToken() {
        return authApiClient.post()
                .uri(properties.getAuthProvider().getTokenPath())
                .bodyValue(Map.of(
                    "client_id", properties.getAuthProvider().getClientId(),
                    "client_secret", properties.getAuthProvider().getClientSecret(),
                    "grant_type", "client_credentials"
                ))
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .doOnNext(res -> log.info("Successfully fetched new OAuth2 token"))
                .doOnError(ex -> log.error("Failed to fetch OAuth2 token: {}", ex.getMessage()));
    }

    public ExchangeFilterFunction filter() {
        return (request, next) -> tokenCache.get()
                .flatMap(token -> {

                    ClientRequest newRequest = ClientRequest.from(request)
                            .header(AUTHORIZATION, "Bearer " + token)
                            .build();

                    return next.exchange(newRequest)
                            .flatMap(response -> {
                                // หากเกิด 401 Unauthorized ให้ทำการ Reset Cache เพื่อขอ Token ใหม่ทันที
                                // กันไว้เฉยๆ ซึ่งไม่ควรเกิดเพราะเราขอ token ใหม้ก่อนหมดอายุ 1 นาที
                                if (response.statusCode().value() == 401) {
                                    log.warn("Detected 401 Unauthorized. Invalidating token cache...");
                                    tokenCache.set(createTokenMono());
                                    return Mono.error(new InternalError("Token expired"));
                                }
                                return Mono.just(response);
                            })
                            .retry(1);
                });
    }

}
