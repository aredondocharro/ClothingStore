package com.aredondocharro.ClothingStore.identityTEST.infrastructure.in.web.error;

import com.aredondocharro.ClothingStore.identity.domain.exception.EmailNotVerifiedException;
import com.aredondocharro.ClothingStore.identity.domain.exception.InvalidCredentialsException;
import com.aredondocharro.ClothingStore.identity.domain.port.in.DeleteUserUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.in.LoginUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.in.RegisterUserUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.in.VerifyEmailUseCase;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.AuthController;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.RefreshCookieManager;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.error.IdentityGlobalErrorHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ IdentityGlobalErrorHandler.class, IdentityErrorHandlerWebMvcAuthControllerTest.Cfg.class })
class IdentityErrorHandlerWebMvcAuthControllerTest {

    @Autowired MockMvc mvc;

    // AuthController requiere estos use cases:
    @MockBean RegisterUserUseCase registerUC;
    @MockBean LoginUseCase loginUC;
    @MockBean VerifyEmailUseCase verifyUC;
    @MockBean DeleteUserUseCase deleteUserUC;

    @TestConfiguration
    static class Cfg {
        @Bean RefreshCookieManager refreshCookieManager() {
            // valores de prueba (no afectan al test porque aquí disparamos errores antes de usar cookie)
            return new RefreshCookieManager(1209600L, true, "Strict", "/auth", "");
        }
    }

    @Test
    void invalid_credentials_is_401_with_code_and_www_authenticate() throws Exception {
        when(loginUC.login(any(), any()))
                .thenThrow(new InvalidCredentialsException("Invalid credentials"));

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"user@example.com","password":"wrong"}
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(jsonPath("$.code").value("identity.invalid_credentials"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/auth/login"));
    }

    @Test
    void email_not_verified_is_403_with_code() throws Exception {
        when(loginUC.login(any(), any()))
                .thenThrow(new EmailNotVerifiedException());

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"user@example.com","password":"Secret123!"}
                        """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("identity.email_not_verified"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/auth/login"));
    }
}
