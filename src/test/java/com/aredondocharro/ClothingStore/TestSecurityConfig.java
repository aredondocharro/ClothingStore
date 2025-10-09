package com.aredondocharro.ClothingStore.testconfig;

import com.aredondocharro.ClothingStore.identity.infrastructure.out.jwt.JwtTokenVerifierAdapter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpStatus;

@TestConfiguration
@EnableMethodSecurity
public class TestSecurityConfig {

    // Bean simulado del validador JWT (no valida nada real, solo evita fallos)
    @Bean
    JwtTokenVerifierAdapter jwtTokenVerifierAdapter() {
        return new JwtTokenVerifierAdapter("test-secret", "AUTH0JWT-BACKEND");
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .requestCache(rc -> rc.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .httpBasic(basic -> {}) // solo devuelve 401 si no hay auth
                .build();
    }
}
