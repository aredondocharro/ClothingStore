package com.aredondocharro.ClothingStore.identityTEST.infrastructure.in.web.error;

import com.aredondocharro.ClothingStore.identity.domain.exception.*;
import com.aredondocharro.ClothingStore.identity.domain.model.Email;
import com.aredondocharro.ClothingStore.identity.domain.port.in.*;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.AuthController;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.RefreshController;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.RefreshCookieManager;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests que ejercitan IdentityGlobalErrorHandler a través de endpoints reales.
 */
@WebMvcTest(controllers = {
        AuthController.class,
        RefreshController.class,
        IdentityErrorHandlerTest.ThrowingController.class
})
@AutoConfigureMockMvc(addFilters = false)
class IdentityErrorHandlerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    RegisterUserUseCase registerUC;
    @MockBean
    LoginUseCase loginUC;
    @MockBean
    VerifyEmailUseCase verifyUC;
    @MockBean
    RefreshCookieManager cookieManager; // usado por AuthController
    @MockBean
    RefreshAccessTokenUseCase refreshUC;
    @MockBean
    LogoutUseCase logoutUC;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    @DisplayName("login -> InvalidCredentialsException -> 401 identity.invalid_credentials")
    void login_invalidCredentials_401() throws Exception {
        when(loginUC.login(any(), anyString()))
                .thenThrow(new InvalidCredentialsException());

        String json = "{\"email\":\"user@example.com\",\"password\":\"Secret123!\"}";

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
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
        // Ajusta el número de argumentos según tu use case concreto.
        doThrow(new EmailAlreadyExistException())
                .when(registerUC).register(any(), anyString(), anyString());

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
        when(verifyUC.verify(eq("bad")))
                .thenThrow(new VerificationTokenInvalidException("Invalid or expired token"));

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
        // En este punto el DispatcherServlet lanza 405 antes de llegar al Advice filtrado por basePackageClasses.
        mvc.perform(get("/auth/login"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("JWTVerificationException desde /auth/login -> 400 con code válido (jwt_invalid o verification_token_invalid)")
    void jwt_verification_exception_400() throws Exception {
        when(loginUC.login(any(), anyString()))
                .thenThrow(new JWTVerificationException("JWT invalid"));

        String json = "{\"email\":\"user@example.com\",\"password\":\"Secret123!\"}";

        MvcResult result = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        JsonNode root = MAPPER.readTree(body);
        String code = root.get("code").asText();

        boolean ok = "identity.jwt_invalid".equals(code) || "identity.verification_token_invalid".equals(code);
        assertTrue(ok, "code debe ser identity.jwt_invalid o identity.verification_token_invalid pero fue: " + code);
    }

    @Test
    @DisplayName("register -> InvalidPasswordException -> 400 identity.invalid_password")
    void invalid_password_400() throws Exception {
        doThrow(new InvalidPasswordException("Password does not meet policy"))
                .when(registerUC).register(any(), anyString(), anyString());

        String json = "{\"name\":\"John\",\"email\":\"user@example.com\",\"password\":\"bad\",\"confirmPassword\":\"bad\"}";

        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("identity.invalid_password"));
    }

    @Test
    @DisplayName("register -> PasswordMismatchException -> 400 identity.password_mismatch")
    void password_mismatch_400() throws Exception {
        doThrow(new PasswordMismatchException())
                .when(registerUC).register(any(), anyString(), anyString());

        String json = "{\"name\":\"John\",\"email\":\"user@example.com\",\"password\":\"a\",\"confirmPassword\":\"b\"}";

        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("identity.password_mismatch"));
    }

    @RestController
    static class ThrowingController {
        @GetMapping("/__test/identity/jwt")
        public void jwt() { throw new JWTVerificationException("JWT invalid"); }
    }
}
