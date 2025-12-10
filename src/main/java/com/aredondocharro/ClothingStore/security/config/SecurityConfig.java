package com.aredondocharro.ClothingStore.security.config;

import com.aredondocharro.ClothingStore.security.filter.AccessTokenFilter;
import com.aredondocharro.ClothingStore.security.port.AccessTokenVerifierPort;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public AccessTokenFilter accessTokenFilter(AccessTokenVerifierPort verifier) {
        return new AccessTokenFilter(verifier);
    }
    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ObjectProvider<AccessTokenFilter> accessTokenFilterProvider,
            @Value("${app.cors.enabled:false}") boolean corsEnabled
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> {
                            res.setStatus(HttpStatus.UNAUTHORIZED.value());
                            res.setContentType("application/json");
                            res.getWriter().write(String.format(
                                    "{\"timestamp\":\"%s\",\"status\":401,\"error\":\"Unauthorized\",\"code\":\"security.unauthorized\",\"message\":\"Authentication required\",\"path\":\"%s\"}",
                                    Instant.now(), req.getRequestURI()));
                        })
                        .accessDeniedHandler((req, res, ex) -> {
                            res.setStatus(HttpStatus.FORBIDDEN.value());
                            res.setContentType("application/json");
                            res.getWriter().write(String.format(
                                    "{\"timestamp\":\"%s\",\"status\":403,\"error\":\"Forbidden\",\"code\":\"security.forbidden\",\"message\":\"Access denied\",\"path\":\"%s\"}",
                                    Instant.now(), req.getRequestURI()));
                        })
                )
                .headers(h -> h
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).preload(true).maxAgeInSeconds(31536000))
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; frame-ancestors 'none'; object-src 'none'"))
                )
                .authorizeHttpRequests(auth -> auth
                        // --- SOLO endpoints realmente públicos en /auth ---
                        .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login", "/auth/verify/resend").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/auth/verify").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/password/forgot", "/auth/password/reset").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/refresh", "/auth/logout").permitAll()

                        // Swagger
                        .requestMatchers("/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html").permitAll()

                        // (Opcional pero práctico) preflight CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Admin por URL
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Todo lo demás → autenticado (401 si no hay token)
                        .anyRequest().authenticated()
                );

        if (corsEnabled) {
            http.cors(Customizer.withDefaults());
        }

        accessTokenFilterProvider.ifAvailable(f ->
                http.addFilterBefore(f, UsernamePasswordAuthenticationFilter.class)
        );

        return http.build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.cors.enabled", havingValue = "true")
    CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:}") String originsCsv) {

        CorsConfiguration c = new CorsConfiguration();
        List<String> origins = Arrays.stream(originsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        c.setAllowedOrigins(origins);
        c.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        c.setAllowedHeaders(List.of("Authorization","Content-Type","X-Requested-With"));
        c.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", c);
        return src;
    }
}
