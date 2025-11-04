package com.aredondocharro.ClothingStore.identityTEST.infrastructure.in.web;

import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.in.ChangePasswordUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.in.RequestPasswordResetUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.in.ResetPasswordUseCase;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.AuthPasswordController;
import com.aredondocharro.ClothingStore.testconfig.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;

import static com.aredondocharro.ClothingStore.security.support.SecurityTestUtils.authPrincipal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthPasswordController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(TestSecurityConfig.class)
class AuthPasswordChangeSecurityTest {

    @Autowired MockMvc mvc;

    @MockitoBean RequestPasswordResetUseCase requestReset;
    @MockitoBean ResetPasswordUseCase resetPassword;
    @MockitoBean ChangePasswordUseCase changePassword;

    @Test
    void change_requires_authentication_returns401() throws Exception {
        String json = """
            { "currentPassword": "Old123!", "newPassword": "NewSecret123!", "confirmNewPassword": "NewSecret123!" }
        """;

        mvc.perform(post("/auth/password/change")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void change_with_authenticated_user_returns204_and_calls_usecase() throws Exception {
        String userId = "11111111-1111-1111-1111-111111111111";

        String json = """
            { "currentPassword": "Old123!", "newPassword": "NewSecret123!", "confirmNewPassword": "NewSecret123!" }
        """;

        mvc.perform(post("/auth/password/change")
                        .with(authPrincipal(userId, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNoContent());

        ArgumentCaptor<UserId> captor = ArgumentCaptor.forClass(UserId.class);
        verify(changePassword).change(captor.capture(), eq("Old123!"), eq("NewSecret123!"));
        assertThat(captor.getValue().value()).isEqualTo(UUID.fromString(userId));
    }
}
