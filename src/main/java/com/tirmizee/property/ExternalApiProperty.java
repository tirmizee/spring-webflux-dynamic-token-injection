package com.tirmizee.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "external-services")
public class ExternalApiProperty {

    private AuthProvider authProvider;
    private ProductApi productApi;

    @Data
    public static class AuthProvider {
        private String baseUrl;
        private String tokenPath;
        private String clientId;
        private String clientSecret;
    }

    @Data
    public static class ProductApi {
        private String baseUrl;
        private String getProductPath;
        private String updateInventoryPath;
    }
}
