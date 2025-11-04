package com.aredondocharro.ClothingStore.identityTEST.infrastructure.in.web.error;

import com.aredondocharro.ClothingStore.identity.domain.exception.*;
import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;
import com.aredondocharro.ClothingStore.identity.domain.port.in.*;
import com.aredondocharro.ClothingStore.identity.domain.port.out.error.VerificationTokenInvalidException;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.AuthController;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.RefreshController;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.RefreshCookieManager;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.error.IdentityGlobalErrorHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests que ejercitan IdentityGlobalErrorHandler a través de endpoints reales.
 * Nota: seguridad desactivada en el slice; 401/403 por access token se prueban en security.
 */
@WebMvcTest(controllers = {
        AuthController.class,
        RefreshController.class
})
@AutoConfigureMockMvc(addFilters = false)
@Import(IdentityGlobalErrorHandler.class)
class IdentityErrorHandlerTest {

    @Autowired MockMvc mvc;

    // ==== Mocks de TODOS los puertos usados por los controllers ====
    @MockitoBean RegisterUserUseCase registerUC;
    @MockitoBean LoginUseCase loginUC;
    @MockitoBean VerifyEmailUseCase verifyUC;
    @MockitoBean DeleteUserUseCase deleteUserUC;
    @MockitoBean RequestPasswordResetUseCase requestResetUC;
    @MockitoBean ResetPasswordUseCase resetPasswordUC;
    @MockitoBean ChangePasswordUseCase changePasswordUC;
    @MockitoBean RefreshAccessTokenUseCase refreshUC;
    @MockitoBean LogoutUseCase logoutUC;
    @MockitoBean RefreshCookieManager cookieManager;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    @DisplayName("login -> InvalidCredentialsException -> 401 identity.invalid_credentials (+WWW-Authenticate)")
    void login_invalidCredentials_401() throws Exception {
        when(loginUC.login(eq(IdentityEmail.of("user@example.com")), eq("Secret123!")))
                .thenThrow(new InvalidCredentialsException("identity.invalid_credentials"));

        String json = "{\"email\":\"user@example.com\",\"password\":\"Secret123!\"}";

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, org.hamcrest.Matchers.containsString("Bearer")))
                .andExpect(jsonPath("$.code").value("identity.invalid_credentials"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.path").value("/auth/login"));
    }

    @Test
    @DisplayName("login -> EmailNotVerifiedException -> 403 identity.email_not_verified")
    void login_emailNotVerified_403() throws Exception {
        when(loginUC.login(any(), anyString()))
                .thenThrow(new EmailNotVerifiedException());

        String json = "{\"email\":\"user@example.com\",\"password\":\"Secret123!\"}";

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("identity.email_not_verified"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("register -> EmailAlreadyExistException -> 409 identity.email_already_exists")
    void register_emailAlreadyExists_409() throws Exception {
        doThrow(new EmailAlreadyExistException())
                .when(registerUC).register(any(IdentityEmail.class), anyString(), anyString());

        String json = "{\"name\":\"John\",\"email\":\"user@example.com\",\"password\":\"Secret123!\",\"confirmPassword\":\"Secret123!\"}";

        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("identity.email_already_exists"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @DisplayName("verify -> VerificationTokenInvalidException -> 400 identity.verification_token_invalid")
    void verify_invalidToken_400() throws Exception {
        when(verifyUC.verify(eq("bad"))).thenThrow(new VerificationTokenInvalidException("Invalid or expired token"));

        mvc.perform(get("/auth/verify").param("token", "bad"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("identity.verification_token_invalid"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("verify -> MissingServletRequestParameterException -> 400 identity.missing_parameter")
    void verify_missingParam_400() throws Exception {
        mvc.perform(get("/auth/verify")) // falta token
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("identity.missing_parameter"));
    }

    @Test
    @DisplayName("register -> MethodArgumentNotValidException -> 400 identity.validation_error (con fieldErrors)")
    void register_validationError_400() throws Exception {
        String json = "{\"name\":\"John\",\"email\":\"not-an-email\",\"password\":\"\",\"confirmPassword\":\"\"}";

        MvcResult result = mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("identity.validation_error"))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        JsonNode root = MAPPER.readTree(body);
        JsonNode fieldErrors = root.get("fieldErrors");

        assertNotNull(fieldErrors, "fieldErrors no debería ser null");
        assertTrue(fieldErrors.isArray(), "fieldErrors debería ser un array");
        assertFalse(fieldErrors.isEmpty(), "fieldErrors debería contener al menos un error");
    }

    @Test
    @DisplayName("login -> JSON malformado -> 400 identity.bad_request")
    void login_unreadableJson_400() throws Exception {
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{")) // JSON roto
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("identity.bad_request"));
    }

    @Test
    @DisplayName("login -> método no permitido (GET) -> 405 (sin body JSON)")
    void login_methodNotAllowed_405() throws Exception {
        mvc.perform(get("/auth/login"))
                .andExpect(status().isMethodNotAllowed());
    }
}
