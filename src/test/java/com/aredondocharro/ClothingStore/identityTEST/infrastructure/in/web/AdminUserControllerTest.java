package com.aredondocharro.ClothingStore.identityTEST.infrastructure.in.web;

import com.aredondocharro.ClothingStore.identity.domain.exception.CannotRemoveLastAdminException;
import com.aredondocharro.ClothingStore.identity.domain.exception.SelfDemotionForbiddenException;
import com.aredondocharro.ClothingStore.identity.domain.exception.UserNotFoundException;
import com.aredondocharro.ClothingStore.identity.domain.model.Role;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.in.DeleteUserUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.in.UpdateUserRolesUseCase;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.AdminUserController;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.error.IdentityGlobalErrorHandler;
import com.aredondocharro.ClothingStore.security.config.SecurityConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;
import java.util.Set;
import java.util.UUID;
import com.aredondocharro.ClothingStore.testconfig.TestSecurityConfig;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Admin endpoints — security & error handling tests.
 * Uses a minimal test SecurityFilterChain and enables @PreAuthorize.
 */
@WebMvcTest(controllers = AdminUserController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import({IdentityGlobalErrorHandler.class,TestSecurityConfig.class})
class AdminUserControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean DeleteUserUseCase deleteUC;
    @MockBean UpdateUserRolesUseCase rolesUC;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // ----------------------------
    // DELETE /admin/users/{id}
    // ----------------------------

    @Test
    @DisplayName("DELETE /admin/users/{id} -> 401 when unauthenticated")
    void delete_unauthenticated_401() throws Exception {
        UUID id = UUID.randomUUID();

        mvc.perform(delete("/admin/users/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /admin/users/{id} -> 403 when authenticated but not ADMIN")
    void delete_forbidden_403() throws Exception {
        UUID id = UUID.randomUUID();

        mvc.perform(delete("/admin/users/{id}", id)
                        .with(user("jane").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /admin/users/{id} -> 204 when ADMIN and user exists")
    void delete_admin_success_204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(deleteUC).delete(UserId.of(id));

        mvc.perform(delete("/admin/users/{id}", id)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /admin/users/{id} -> 404 identity.user_not_found")
    void delete_admin_not_found_404() throws Exception {
        UserId id = UserId.newId();
        doThrow(new UserNotFoundException(id)).when(deleteUC).delete(id);

        mvc.perform(delete("/admin/users/{id}", id)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("identity.user_not_found"));
    }

    // ----------------------------
    // PUT /admin/users/{id}/roles
    // ----------------------------

    @Test
    @DisplayName("PUT /admin/users/{id}/roles -> 401 when unauthenticated")
    void setRoles_unauthenticated_401() throws Exception {
        UUID id = UUID.randomUUID();
        String json = "{ \"roles\": [\"USER\"] }";

        mvc.perform(put("/admin/users/{id}/roles", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /admin/users/{id}/roles -> 403 when authenticated but not ADMIN")
    void setRoles_forbidden_403() throws Exception {
        UUID id = UUID.randomUUID();
        String json = "{ \"roles\": [\"USER\"] }";

        mvc.perform(put("/admin/users/{id}/roles", id)
                        .with(user("jane").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /admin/users/{id}/roles -> 204 when ADMIN and valid payload")
    void setRoles_admin_success_204() throws Exception {
        UUID id = UUID.randomUUID();
        String json = "{ \"roles\": [\"USER\", \"ADMIN\"] }";

        doNothing().when(rolesUC).setRoles(UserId.of(id), Set.of(Role.USER, Role.ADMIN));

        mvc.perform(put("/admin/users/{id}/roles", id)
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PUT /admin/users/{id}/roles -> 400 identity.validation_error when roles is empty")
    void setRoles_validation_error_400() throws Exception {
        UUID id = UUID.randomUUID();
        String json = "{ \"roles\": [] }";

        MvcResult result = mvc.perform(put("/admin/users/{id}/roles", id)
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("identity.validation_error"))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        JsonNode root = MAPPER.readTree(body);
        JsonNode fieldErrors = root.get("fieldErrors");

        assertNotNull(fieldErrors);
        assertTrue(fieldErrors.isArray());
        assertTrue(fieldErrors.size() > 0);
    }

    @Test
    @DisplayName("PUT /admin/users/{id}/roles -> 400 identity.bad_request when body is missing/invalid JSON")
    void setRoles_bad_request_400() throws Exception {
        UUID id = UUID.randomUUID();

        mvc.perform(put("/admin/users/{id}/roles", id)
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{")) // malformed
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("identity.bad_request"));
    }

    @Test
    @DisplayName("PUT /admin/users/{id}/roles -> 404 identity.user_not_found")
    void setRoles_not_found_404() throws Exception {
        UserId id = UserId.newId();
        String json = "{ \"roles\": [\"USER\"] }";

        doThrow(new UserNotFoundException(id)).when(rolesUC).setRoles(id, Set.of(Role.USER));

        mvc.perform(put("/admin/users/{id}/roles", id)
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("identity.user_not_found"));
    }

    @Test
    @DisplayName("PUT /admin/users/{id}/roles -> 409 identity.cannot_remove_last_admin")
    void setRoles_cannot_remove_last_admin_409() throws Exception {
        UUID id = UUID.randomUUID();
        String json = "{ \"roles\": [\"USER\"] }";

        doThrow(new CannotRemoveLastAdminException()).when(rolesUC).setRoles(UserId.of(id), Set.of(Role.USER));

        mvc.perform(put("/admin/users/{id}/roles", id)
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("identity.cannot_remove_last_admin"));
    }

    @Test
    @DisplayName("PUT /admin/users/{id}/roles -> 403 identity.self_demotion_forbidden")
    void setRoles_self_demotion_forbidden_403() throws Exception {
        UUID id = UUID.randomUUID();
        String json = "{ \"roles\": [\"USER\"] }";

        doThrow(new SelfDemotionForbiddenException()).when(rolesUC).setRoles(UserId.of(id), Set.of(Role.USER));

        mvc.perform(put("/admin/users/{id}/roles", id)
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("identity.self_demotion_forbidden"));
    }

}
