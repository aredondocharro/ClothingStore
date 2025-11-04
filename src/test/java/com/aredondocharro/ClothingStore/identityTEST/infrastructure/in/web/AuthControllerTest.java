package com.aredondocharro.ClothingStore.identityTEST.infrastructure.in.web;

import com.aredondocharro.ClothingStore.identity.domain.exception.InvalidCredentialsException;
import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;
import com.aredondocharro.ClothingStore.identity.domain.port.in.*;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.AuthController;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.RefreshCookieManager;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.error.IdentityGlobalErrorHandler;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // sin filtros de seguridad en este slice
@Import(IdentityGlobalErrorHandler.class)  // para mapear 400/401/409 de Identity
class AuthControllerTest {

    @Autowired MockMvc mvc;

    @MockitoBean RegisterUserUseCase registerUC;
    @MockitoBean LoginUseCase loginUC;
    @MockitoBean VerifyEmailUseCase verifyUC;
    @MockitoBean DeleteUserUseCase deleteUserUC;
    @MockitoBean RefreshCookieManager cookieManager;

    @Test
    void login_setsRefreshCookie_andReturnsAccessInBody() throws Exception {
        when(loginUC.login(any(IdentityEmail.class), eq("Secret123!")))
                .thenReturn(new AuthResult("ACCESS", "REFRESH_COOKIE"));

        var json = """
                {"email":"user@example.com","password":"Secret123!"}
                """;

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("ACCESS"))
                .andExpect(jsonPath("$.refreshToken", Matchers.nullValue()));

        verify(loginUC).login(eq(IdentityEmail.of("user@example.com")), eq("Secret123!"));
        verify(cookieManager).setCookie(any(HttpServletResponse.class), eq("REFRESH_COOKIE"));
    }

    @Test
    void verify_setsRefreshCookie_andReturnsAccessInBody() throws Exception {
        when(verifyUC.verify(eq("VERIF_TOKEN")))
                .thenReturn(new AuthResult("ACCESS_V", "REFRESH_V"));

        mvc.perform(get("/auth/verify").param("token", "VERIF_TOKEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("ACCESS_V"))
                .andExpect(jsonPath("$.refreshToken", Matchers.nullValue()));

        verify(verifyUC).verify("VERIF_TOKEN");
        verify(cookieManager).setCookie(any(HttpServletResponse.class), eq("REFRESH_V"));
    }

    @Test
    void login_withInvalidCredentials_returns401_and_WWWAuthenticate() throws Exception {
        when(loginUC.login(eq(IdentityEmail.of("user@example.com")), eq("badpass")))
                .thenThrow(new InvalidCredentialsException("identity.invalid_credentials"));

        var json = """
                {"email":"user@example.com","password":"badpass"}
                """;

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, Matchers.containsString("Bearer")))
                .andExpect(jsonPath("$.code").value("identity.invalid_credentials"));
    }

    @Test
    void verify_without_token_param_returns400() throws Exception {
        mvc.perform(get("/auth/verify"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("identity.missing_parameter"));
    }
}
