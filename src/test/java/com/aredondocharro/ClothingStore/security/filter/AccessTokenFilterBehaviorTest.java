package com.aredondocharro.ClothingStore.security.filter;

import com.aredondocharro.ClothingStore.security.port.AccessTokenVerifierPort;
import com.aredondocharro.ClothingStore.security.port.AuthPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AccessTokenFilterBehaviorTest {

    @RestController
    static class WhoAmIController {
        @GetMapping("/whoami")
        public Map<String,Object> who() {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            Map<String,Object> out = new HashMap<>();
            out.put("authenticated", auth != null && auth.isAuthenticated());
            out.put("name", auth != null ? auth.getName() : null);
            return out;
        }
    }

    private final AccessTokenVerifierPort verifier = mock(AccessTokenVerifierPort.class);
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        var filter = new AccessTokenFilter(verifier);
        mvc = standaloneSetup(new WhoAmIController()).addFilters(filter).build();
    }

    @Test
    void no_token_keeps_request_unauthenticated() throws Exception {
        mvc.perform(get("/whoami"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(false))
                .andExpect(jsonPath("$.name").doesNotExist());
        verifyNoInteractions(verifier);
    }

    @Test
    void invalid_token_keeps_request_unauthenticated() throws Exception {
        when(verifier.verify("bad")).thenReturn(null);
        mvc.perform(get("/whoami").header("Authorization", "Bearer bad"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(false));
        verify(verifier).verify("bad");
    }

    @Test
    void valid_token_sets_authentication() throws Exception {
        AuthPrincipal principal = mock(AuthPrincipal.class);
        when(principal.userId()).thenReturn("u-1"); // adapta si tu AuthPrincipal difiere
        when(verifier.verify("good")).thenReturn(principal);

        mvc.perform(get("/whoami").header("Authorization", "Bearer good"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true));
        verify(verifier).verify("good");
    }
}
