package com.aredondocharro.ClothingStore.identity.infrastructure.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Schema(description = "Register Request DTO")
@Getter @Setter @ToString(exclude = {"password", "confirmPassword"})
public class RegisterRequest {


        @NotBlank @Email
        @Schema(example = "user@example.com")
        private String email;

        @NotBlank
        @Size(min = 8, max = 72) // 72 por compatibilidad con BCrypt

        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,72}$", // al menos una minúscula, una mayúscula y un dígito
                  message = "password must contain upper, lower and digit")
        @Schema(example = "Secret123!")
        private String password;

        @NotBlank
        @Schema(example = "Secret123!")
        private String confirmPassword;

        @AssertTrue(message = "passwords do not match")
        public boolean isPasswordsMatch() {
                // si falta alguna, deja que fallen sus @NotBlank y NO dispares este error
                if (password == null || confirmPassword == null) return true;
                if (password.isBlank() || confirmPassword.isBlank()) return true;
                return password.equals(confirmPassword);
        }
}

