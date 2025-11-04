package com.aredondocharro.ClothingStore.identityTEST.infrastructure.in.web.error;

import com.aredondocharro.ClothingStore.identity.domain.exception.CannotRemoveLastAdminException;
import com.aredondocharro.ClothingStore.identity.domain.exception.UserNotFoundException;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.in.DeleteUserUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.in.UpdateUserRolesUseCase;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.AdminUserController;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.error.IdentityGlobalErrorHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(IdentityGlobalErrorHandler.class)
class IdentityErrorHandlerWebMvcAdminControllerTest {

    @Autowired MockMvc mvc;

    @MockitoBean UpdateUserRolesUseCase rolesUC;
    @MockitoBean DeleteUserUseCase deleteUserUC;

    @Test
    void delete_non_existing_user_is_404_with_code() throws Exception {
        UUID id = UUID.fromString("11111111-2222-3333-4444-555555555555");
        doThrow(new UserNotFoundException(UserId.of(id)))
                .when(deleteUserUC).delete(UserId.of(id));

        mvc.perform(delete("/admin/users/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("identity.user_not_found"))
                .andExpect(jsonPath("$.path").value("/admin/users/" + id));
    }

    @Test
    void cannot_remove_last_admin_is_409_with_code() throws Exception {
        UUID id = UUID.fromString("11111111-2222-3333-4444-555555555555");
        doThrow(new CannotRemoveLastAdminException())
                .when(rolesUC).setRoles(any(UserId.class), any(Set.class));

        mvc.perform(put("/admin/users/{id}/roles", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\":[\"USER\"]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("identity.cannot_remove_last_admin"))
                .andExpect(jsonPath("$.path").value("/admin/users/" + id + "/roles"));
    }
}
