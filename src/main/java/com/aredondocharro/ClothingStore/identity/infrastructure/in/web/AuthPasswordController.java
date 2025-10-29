package com.aredondocharro.ClothingStore.identity.infrastructure.in.web;

import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.in.ChangePasswordUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.in.RequestPasswordResetUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.in.ResetPasswordUseCase;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.dto.ChangePasswordRequest;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.dto.ForgotPasswordRequest;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.dto.ResetPasswordRequest;
import com.aredondocharro.ClothingStore.security.port.AuthPrincipal;
import com.aredondocharro.ClothingStore.shared.log.LogSanitizer;
import com.aredondocharro.ClothingStore.shared.web.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.aredondocharro.ClothingStore.shared.log.LogSanitizer.maskEmail;

@Tag(name = "Password", description = "Forgot / Reset / Change")
@RestController
@RequestMapping(path = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class AuthPasswordController {

    private final RequestPasswordResetUseCase requestReset;
    private final ResetPasswordUseCase resetPassword;
    private final ChangePasswordUseCase changePassword;

    public AuthPasswordController(RequestPasswordResetUseCase requestReset,
                                  ResetPasswordUseCase resetPassword,
                                  ChangePasswordUseCase changePassword) {
        this.requestReset = requestReset;
        this.resetPassword = resetPassword;
        this.changePassword = changePassword;
    }

    @Operation(
            summary = "Forgot: request a reset email",
            description = "Always returns 202 to prevent user enumeration.",
            security = {} // público en la doc (anula el requirement global)
    )
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ForgotPasswordRequest.class),
                    examples = @ExampleObject(
                            name = "forgot",
                            value = """
                    { "email": "user@example.com" }
                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "202",
                    description = "If the email exists, instructions are sent.",
                    content = @Content(schema = @Schema(hidden = true)) // 202: sin body
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error (e.g. invalid email format)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                    { "code": "validation.error", "message": "Invalid email" }
                    """)
                    )
            )
    })
    @PostMapping(value = "/password/forgot", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> forgot(@Valid @org.springframework.web.bind.annotation.RequestBody ForgotPasswordRequest req) {
        IdentityEmail email = IdentityEmail.of(req.email());
        log.debug("Password FORGOT requested for email={}", maskEmail(email.getValue()));
        requestReset.requestReset(email);
        log.info("Password FORGOT accepted for email={} (anti-enumeration 202)", maskEmail(email.getValue()));
        return ResponseEntity.accepted().build();
    }

    @Operation(
            summary = "Reset: apply a new password using the token",
            security = {} // público en la doc (anula el requirement global)
    )
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResetPasswordRequest.class),
                    examples = @ExampleObject(
                            name = "reset",
                            value = """
                    { "newPassword": "NewSecret123!", "confirmNewPassword": "NewSecret123!" }
                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Password successfully reset.",
                    content = @Content(schema = @Schema(hidden = true)) // 204: sin body
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired token",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                { "code": "identity.invalid_or_expired_token", "message": "Invalid or expired token" }
                """)
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Password policy violation",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                { "code": "identity.password_policy_violation", "message": "Password does not meet policy requirements" }
                """)
                    )
            )
    })
    @PostMapping(value = "/password/reset", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> reset(
            @Parameter(description = "Password reset token", example = "Vnpuft***")
            @RequestParam("token") String token,
            @Valid @org.springframework.web.bind.annotation.RequestBody ResetPasswordRequest req) {

        log.debug("Password RESET called with token={}...", LogSanitizer.maskToken(token));
        resetPassword.reset(token, req.newPassword());
        log.info("Password RESET completed (token consumed)");
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Change: change password (authenticated user)"
            // Con seguridad global ya basta; puedes dejar o quitar el siguiente requisito explícito:
            , security = @SecurityRequirement(name = "bearerAuth")
    )
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ChangePasswordRequest.class),
                    examples = @ExampleObject(
                            name = "change",
                            value = """
                    { "currentPassword": "Old123!", "newPassword": "NewSecret123!", "confirmNewPassword": "NewSecret123!" }
                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Password changed.",
                    content = @Content(schema = @Schema(hidden = true)) // 204: sin body
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Current password is incorrect.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                { "code": "identity.current_password_incorrect", "message": "Current password is incorrect" }
                """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                { "code": "security.unauthorized", "message": "Authentication required" }
                """)
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Password policy violation",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                { "code": "identity.password_policy_violation", "message": "Password does not meet policy requirements" }
                """)
                    )
            )
    })
    @PostMapping(value = "/password/change", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> change(@AuthenticationPrincipal AuthPrincipal me,
                                       @Valid @org.springframework.web.bind.annotation.RequestBody ChangePasswordRequest req) {
        UserId userId = UserId.of(UUID.fromString(me.userId()));
        log.debug("Password CHANGE requested by userId={}", userId);
        changePassword.change(userId, req.currentPassword(), req.newPassword());
        log.info("Password CHANGE completed for userId={}", userId);
        return ResponseEntity.noContent().build();
    }
}
