package com.aredondocharro.ClothingStore.identityTEST.infrastructure.in.web;

import com.aredondocharro.ClothingStore.identity.domain.port.in.AuthResult;
import com.aredondocharro.ClothingStore.identity.domain.port.in.LogoutUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.in.RefreshAccessTokenUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.out.error.RefreshSessionInvalidException;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.RefreshController;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.RefreshCookieManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.SET_COOKIE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RefreshController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(RefreshControllerCookieTest.Cfg.class)
class RefreshControllerCookieTest {

    @Autowired MockMvc mvc;

    @MockitoBean RefreshAccessTokenUseCase refreshUC;
    @MockitoBean LogoutUseCase            logoutUC;

    @TestConfiguration
    static class Cfg {
        @Bean
        RefreshCookieManager refreshCookieManager() {
            return new RefreshCookieManager(
                    1209600L,   // Max-Age (14 días)
                    true,       // Secure
                    "Strict",   // SameSite
                    "/auth",    // Path
                    ""          // Domain
            );
        }
    }

    @Test
    void rotates_refresh_cookie_and_sets_cookie_attributes() throws Exception {
        when(refreshUC.refresh(anyString(), anyString(), anyString()))
                .thenReturn(new AuthResult("newA", "newR"));

        mvc.perform(post("/auth/refresh")
                        .cookie(new Cookie("refresh_token", "oldR"))
                        .header("User-Agent", "JUnit")) // <- importante
                .andExpect(status().isOk())
                .andExpect(header().string(SET_COOKIE, containsString("refresh_token=newR")))
                .andExpect(header().string(SET_COOKIE, containsString("HttpOnly")))
                .andExpect(header().string(SET_COOKIE, containsString("Secure")))
                .andExpect(header().string(SET_COOKIE, containsString("SameSite=Strict")))
                .andExpect(header().string(SET_COOKIE, containsString("Path=/auth")))
                .andExpect(jsonPath("$.accessToken").value("newA"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist());
    }

    @Test
    void replayed_refresh_is_rejected_with_401() throws Exception {
        when(refreshUC.refresh(anyString(), anyString(), anyString()))
                .thenThrow(new RefreshSessionInvalidException("replayed"));

        mvc.perform(post("/auth/refresh")
                        .cookie(new Cookie("refresh_token", "stale"))
                        .header("User-Agent", "JUnit")) // <- importante
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("identity.refresh_invalid"));
    }
}
