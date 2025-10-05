package com.aredondocharro.ClothingStore.identity.infrastructure.in.web;

import com.aredondocharro.ClothingStore.identity.domain.port.in.ChangePasswordUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.in.RequestPasswordResetUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.in.ResetPasswordUseCase;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.dto.ChangePasswordRequest;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.dto.ForgotPasswordRequest;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.dto.ResetPasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
            description = "Always returns 202 to prevent user enumeration."
    )
    @ApiResponse(responseCode = "202", description = "If the email exists, instructions are sent.")
    @PostMapping(value = "/password/forgot", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> forgot(@Valid @RequestBody ForgotPasswordRequest req) {
        log.debug("Password FORGOT requested for email={}", maskEmail(req.email()));
        requestReset.requestReset(req.email());
        log.info("Password FORGOT accepted for email={} (anti-enumeration response 202)", maskEmail(req.email()));
        return ResponseEntity.accepted().build();
    }

    @Operation(
            summary = "Reset: apply a new password using the token"
    )
    @ApiResponse(responseCode = "204", description = "Password successfully reset.")
    @ApiResponse(responseCode = "400", description = "Invalid or expired token.", content = @Content)
    @PostMapping(value = "/password/reset", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> reset(@RequestParam("token") String token,
                                      @Valid @RequestBody ResetPasswordRequest req) {
        log.debug("Password RESET called with token={}", maskToken(token));
        resetPassword.reset(token, req.newPassword());
        log.info("Password RESET completed (token consumed)");
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Change: change password (authenticated user)"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "204", description = "Password changed.")
    @ApiResponse(responseCode = "400", description = "Current password is incorrect.", content = @Content)
    @PostMapping(value = "/password/change", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> change(Authentication auth,
                                       @Valid @RequestBody ChangePasswordRequest req) {
        // NOTE: if auth.getName() is not a UUID, extract your real user ID from the principal
        UUID userId = UUID.fromString(auth.getName());
        log.debug("Password CHANGE requested by userId={}", userId);
        changePassword.change(userId, req.currentPassword(), req.newPassword());
        log.info("Password CHANGE completed for userId={}", userId);
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------
    private static String maskToken(String token) {
        if (token == null) return "null";
        int len = token.length();
        int keep = Math.min(6, len);
        return token.substring(0, keep) + "*** (len=" + len + ")";
    }
}
