package com.cinq.vestaboard.configuration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${app.security.allowed-domain}")
    private String allowedDomain;

    @Value("${app.security.allowed-origins}")
    private List<String> allowedOrigins;

    @Value("${app.security.admin-emails:}")
    private List<String> adminEmails;

    @Autowired
    private Environment environment;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        boolean isDevProfile = Arrays.asList(environment.getActiveProfiles()).contains("local")
                || Arrays.asList(environment.getActiveProfiles()).contains("dev")
                || environment.getActiveProfiles().length == 0;

        http
                // CORS must be configured here so Spring Security's filter applies it before
                // authentication — preflight OPTIONS requests must not require a token.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(h -> h
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                        .contentTypeOptions(co -> {})
                        .frameOptions(fo -> fo.deny())
                )
                .authorizeHttpRequests(auth -> {
                    if (isDevProfile) {
                        auth.requestMatchers("/api-docs/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll();
                    }
                    auth.requestMatchers(HttpMethod.GET, "/vestaboard/**").hasRole("VIEWER")
                        .requestMatchers(HttpMethod.POST, "/vestaboard/**").hasRole("ADMIN")
                        .anyRequest().hasRole("VIEWER");
                })
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(googleTokenDecoder())
                                .jwtAuthenticationConverter(domainRestrictedConverter())
                        )
                        .authenticationEntryPoint(this::handleAuthenticationFailure)
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((request, response, exception) -> {
                    log.warn("Access denied: method={} uri='{}' reason='{}'",
                            request.getMethod(), request.getRequestURI(), exception.getMessage());
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
                })
                );
        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private JwtDecoder googleTokenDecoder() {
        // Fetches Google's public signing keys from their JWKS endpoint on first request,
        // then caches them. Keys are only re-fetched when an unknown key ID is encountered.
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .build();

        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer("https://accounts.google.com"),
                new JwtClaimValidator<>(JwtClaimNames.AUD, (List<String> aud) -> {
                    boolean valid = aud != null && aud.contains(clientId);
                    if (!valid) {
                        log.warn("JWT audience validation failed: actual aud='{}', expected clientId='{}'", aud, clientId);
                    }
                    return valid;
                })
        );
        decoder.setJwtValidator(validator);
        return decoder;
    }

    private JwtAuthenticationConverter domainRestrictedConverter() {
        // The 'hd' (hosted domain) claim is set by Google for Workspace accounts.
        // It is part of the signed token, so it cannot be spoofed.
        // Only tokens from the configured domain receive ROLE_USER and pass authorization.
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String email = jwt.getClaimAsString("email");
            String hostedDomain = jwt.getClaimAsString("hd");
            if (!allowedDomain.equals(hostedDomain)) {
                log.warn("Authentication rejected: email='{}' has domain '{}', expected '{}'",
                        email, hostedDomain, allowedDomain);
                return List.of();
            }
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_VIEWER"));
            if (adminEmails.contains(email)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                log.info("Authentication granted with ADMIN role: email='{}'", email);
            } else {
                log.info("Authentication granted with VIEWER role: email='{}'", email);
            }
            return authorities;
        });
        return converter;
    }

    private void handleAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                             AuthenticationException ex) throws IOException {
        log.warn("Authentication failed: method={} uri='{}' reason='{}'",
                request.getMethod(), request.getRequestURI(), ex.getMessage());
        new BearerTokenAuthenticationEntryPoint().commence(request, response, ex);
    }
}
