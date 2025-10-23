package com.aredondocharro.ClothingStore.security.filter;

import com.aredondocharro.ClothingStore.security.port.AccessTokenVerifierPort;
import com.aredondocharro.ClothingStore.security.port.AuthPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;                 // <-- nuevo
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AccessTokenFilterTest {

    @Autowired
    private MockMvc mvc;

    // Mock del port que usa el filtro
    @MockBean
    private AccessTokenVerifierPort verifier;

    @TestConfiguration
    static class TestBeans {
        // ⚠️ NO registrar AccessTokenFilter aquí (ya lo crea SecurityConfig)

        // Endpoint de prueba que devuelve el id del principal
        @RestController
        static class TestController {
            @GetMapping("/test/me")
            public String me(@AuthenticationPrincipal AuthPrincipal principal) {
                return principal == null ? "null" : principal.userId();
            }
        }
    }

    @Test
    void returns_200_with_valid_bearer_and_sets_principal() throws Exception {
        String uid = UUID.randomUUID().toString();
        Instant now = Instant.parse("2025-01-01T00:00:00Z");

        // Usa el constructor del record (evita problemas con factory methods)
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
