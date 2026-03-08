package com.tirmizee.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    private Status status;
    private Detail data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Status {
        private int code;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Detail {

        @JsonProperty("accessToken")
        private String accessToken;

        @JsonProperty("tokenType")
        private String tokenType;

        @JsonProperty("expiresIn")
        private int expiresIn;

        @JsonProperty("expiresAt")
        private long expiresAt;
    }
}