package com.aredondocharro.ClothingStore.identityTEST.infrastructure.in.web;


import com.aredondocharro.ClothingStore.identity.domain.port.in.AuthResult;
import com.aredondocharro.ClothingStore.identity.domain.port.in.RefreshAccessTokenUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.in.LogoutUseCase;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.RefreshController;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.RefreshCookieManager;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.error.IdentityGlobalErrorHandler;
import jakarta.servlet.http.Cookie;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.aredondocharro.ClothingStore.testconfig.TestSecurityConfig;


import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RefreshController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
class RefreshControllerHeaderTest {

    @Autowired MockMvc mvc;

    @MockBean RefreshAccessTokenUseCase refreshUC;
    @MockBean LogoutUseCase logoutUC;

    @TestConfiguration
    static class Cfg {
        @Bean
        RefreshCookieManager cookieManager() {
            return new RefreshCookieManager(1209600L, false, "Strict", "/auth", "");
        }
    }

    @Test
    void refresh_rotates_and_sets_new_cookie() throws Exception {
        when(refreshUC.refresh(eq("R1"), anyString(), anyString()))
                .thenReturn(new AuthResult("ACCESS_NEW", "REFRESH_NEW"));

        mvc.perform(post("/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", "R1"))
                        .header("User-Agent", "JUnit")                    // ← añade UA
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("ACCESS_NEW"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(header().string("Set-Cookie",
                        Matchers.containsString("refresh_token=REFRESH_NEW")));
    }

    @Test
    void logout_clears_cookie() throws Exception {
        mvc.perform(post("/auth/logout")
                        .cookie(new Cookie("refresh_token", "R1"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out"))
                .andExpect(header().string("Set-Cookie", Matchers.containsString("Max-Age=0")));
    }
}
