package com.cinq.vestaboard.configuration.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties("app.gateway.vestaboard")
public class VestaBoardProperties {
    @NotBlank
    private String baseUrl;
    @NotBlank
    private String token;
}
