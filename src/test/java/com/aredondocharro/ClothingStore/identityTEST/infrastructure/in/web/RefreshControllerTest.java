package com.aredondocharro.ClothingStore.identityTEST.infrastructure.in.web;

import com.aredondocharro.ClothingStore.identity.domain.port.in.LogoutUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.in.RefreshAccessTokenUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.in.AuthResult;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.dto.MessageResponse;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.RefreshController;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.RefreshCookieManager;
import jakarta.servlet.http.Cookie;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.HttpServletResponse;


import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RefreshController.class)
@AutoConfigureMockMvc(addFilters = false)
class RefreshControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    RefreshAccessTokenUseCase refreshUC;

    @MockBean
    LogoutUseCase logoutUC;

    @MockBean
    RefreshCookieManager cookieManager;

    @Test
    void refresh_returnsNewAccess_andSetsRotatedRefreshCookie() throws Exception {
        when(refreshUC.refresh(eq("R1"), anyString(), anyString()))
                .thenReturn(new AuthResult("ACCESS_NEW", "REFRESH_NEW"));

        mvc.perform(post("/auth/refresh")
                        .cookie(new Cookie("refresh_token", "R1"))
                        .header("User-Agent", "JUnit")               // ← añade UA
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("ACCESS_NEW"))
                .andExpect(jsonPath("$.refreshToken", Matchers.nullValue()));

        verify(refreshUC).refresh(eq("R1"), anyString(), anyString());
        verify(cookieManager).setCookie(any(HttpServletResponse.class), eq("REFRESH_NEW"));
    }

    @Test
    void logout_revokesRefresh_andClearsCookie() throws Exception {
        mvc.perform(post("/auth/logout")
                        .cookie(new Cookie("refresh_token", "R1"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out"));

        verify(logoutUC).logout(eq("R1"), anyString());
        verify(cookieManager).clearCookie(any(HttpServletResponse.class));
    }
}
