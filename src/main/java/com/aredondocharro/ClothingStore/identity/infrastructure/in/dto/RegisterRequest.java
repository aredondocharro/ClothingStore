package com.aredondocharro.ClothingStore.identity.infrastructure.in.dto;

import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordPolicyPort;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Schema(description = "Register Request DTO")
@Getter @Setter
@ToString(exclude = {"password", "confirmPassword"})
public class RegisterRequest {

        @NotBlank @Email
        @Schema(example = "user@example.com")
        private String email;

        @NotBlank
        @Schema(example = "Secret123!")
        private String password;

        @NotBlank
        @Schema(example = "Secret123!")
        private String confirmPassword;

        /** Delegación: el DTO NO conoce reglas, solo invoca la política */
        public void assertValid(PasswordPolicyPort policy) {
                policy.validatePair(password, confirmPassword);
        }
}
