package com.tirmizee.config;

import com.tirmizee.property.ExternalApiProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final OAuth2TokenFilter oAuth2TokenFilter;
    private final ExternalApiProperty externalApiProperty;

    @Bean
    public WebClient externalApiClient(WebClient.Builder builder) {
        return builder
                .baseUrl(externalApiProperty.getProductApi().getBaseUrl())
                .defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .defaultHeader(ACCEPT, APPLICATION_JSON_VALUE)
                .filter(oAuth2TokenFilter.filter())
                .build();
    }

}
