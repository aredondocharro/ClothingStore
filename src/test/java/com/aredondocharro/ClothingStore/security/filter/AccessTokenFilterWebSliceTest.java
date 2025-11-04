package com.aredondocharro.ClothingStore.security.filter;

import com.aredondocharro.ClothingStore.security.port.AccessTokenVerifierPort;
import com.aredondocharro.ClothingStore.security.port.AuthPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TestControllerForAccessTokenFilter.class) // <- registra SOLO el controller de test
@AutoConfigureMockMvc(addFilters = true)                            // <- activa el pipeline de Spring Security
@Import(AccessTokenFilterWebSliceTest.SecurityTestConfig.class)     // <- cadena de seguridad de prueba
class AccessTokenFilterWebSliceTest {

    @Autowired MockMvc mvc;

    @MockitoBean AccessTokenVerifierPort verifier;

    @TestConfiguration
    static class SecurityTestConfig {
        @Bean
        @Order(Ordered.HIGHEST_PRECEDENCE)
        SecurityFilterChain testChain(HttpSecurity http, AccessTokenVerifierPort verifier) throws Exception {
            http
                    .securityMatcher("/test/**")
                    .csrf(csrf -> csrf.disable())
                    .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .securityContext(sc -> sc.securityContextRepository(new NullSecurityContextRepository()))
                    .anonymous(anon -> anon.disable()) // sin anónimo -> si no hay auth => 401
                    .exceptionHandling(e -> e
                            .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                            .accessDeniedHandler((req, res, ex) -> res.setStatus(HttpStatus.UNAUTHORIZED.value()))
                    )
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/test/**").hasAuthority("ROLE_USER")
                            .anyRequest().denyAll()
                    );

            // Inserta tu filtro con el mock del verifier
            http.addFilterBefore(new AccessTokenFilter(verifier), UsernamePasswordAuthenticationFilter.class);
            return http.build();
        }
    }

    @Test
    void returns_200_with_valid_bearer_and_sets_principal() throws Exception {
        String uid = UUID.randomUUID().toString();
        Instant now = Instant.parse("2025-01-01T00:00:00Z");

        when(verifier.verify("good"))
                .thenReturn(new AuthPrincipal(uid, List.of("ROLE_USER"), now, now.plusSeconds(600)));

        mvc.perform(get("/test/me")
                        .header("Authorization", "Bearer good")
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string(uid));
    }

    @Test
    void returns_401_without_token() throws Exception {
        mvc.perform(get("/test/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void returns_401_with_invalid_token() throws Exception {
        when(verifier.verify("bad"))
                .thenThrow(new AccessTokenVerifierPort.InvalidTokenException("x"));

        mvc.perform(get("/test/me")
                        .header("Authorization", "Bearer bad"))
                .andExpect(status().isUnauthorized());
    }
}
