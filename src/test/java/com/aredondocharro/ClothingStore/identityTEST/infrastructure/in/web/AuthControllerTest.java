package com.aredondocharro.ClothingStore.identityTEST.infrastructure.in.web;

import com.aredondocharro.ClothingStore.identity.domain.model.Email;
import com.aredondocharro.ClothingStore.identity.domain.port.in.AuthResult;
import com.aredondocharro.ClothingStore.identity.domain.port.in.LoginUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.in.RegisterUserUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.in.VerifyEmailUseCase;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.AuthController;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.RefreshCookieManager;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean RegisterUserUseCase registerUC;
    @MockBean LoginUseCase loginUC;
    @MockBean VerifyEmailUseCase verifyUC;
    @MockBean
    RefreshCookieManager cookieManager;

    @Test
    void login_setsRefreshCookie_andReturnsAccessInBody() throws Exception {
        when(loginUC.login(any(Email.class), eq("Secret123!")))
                .thenReturn(new AuthResult("ACCESS", "REFRESH_COOKIE"));

        var json = """
                {"email":"user@example.com","password":"Secret123!"}
                """;

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("ACCESS"))
                .andExpect(jsonPath("$.refreshToken", Matchers.nullValue()));

        verify(loginUC).login(eq(Email.of("user@example.com")), eq("Secret123!"));
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
}
