package com.aredondocharro.ClothingStore.identityTEST.infrastructure.in.web;

import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.in.DeleteUserUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.in.LoginUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.in.RegisterUserUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.in.VerifyEmailUseCase;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.AuthController;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.RefreshCookieManager;
import com.aredondocharro.ClothingStore.testconfig.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static com.aredondocharro.ClothingStore.security.support.SecurityTestUtils.authPrincipal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(TestSecurityConfig.class)
class AuthDeleteSecurityTest {

    @Autowired MockMvc mvc;

    @MockBean RegisterUserUseCase registerUC;
    @MockBean LoginUseCase loginUC;
    @MockBean VerifyEmailUseCase verifyUC;
    @MockBean DeleteUserUseCase deleteUC;
    @MockBean RefreshCookieManager cookieManager;

    @Test
    void delete_requires_authentication_returns401() throws Exception {
        mvc.perform(delete("/auth/delete"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void delete_with_authenticated_user_returns204_and_calls_usecase() throws Exception {
        String userId = "11111111-1111-1111-1111-111111111111";

        mvc.perform(delete("/auth/delete")
                        .with(authPrincipal(userId, "USER")))
                .andExpect(status().isNoContent());

        ArgumentCaptor<UserId> captor = ArgumentCaptor.forClass(UserId.class);
        verify(deleteUC).delete(captor.capture());
        assertThat(captor.getValue().value()).isEqualTo(UUID.fromString(userId));
    }
}
