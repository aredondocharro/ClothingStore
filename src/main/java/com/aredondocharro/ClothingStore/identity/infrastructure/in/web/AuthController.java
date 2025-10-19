package com.aredondocharro.ClothingStore.identity.infrastructure.in.web;

import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail; // VO correcto
import com.aredondocharro.ClothingStore.identity.domain.port.in.*;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenVerifierPort;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.dto.AuthResponse;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.dto.LoginRequest;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.dto.MessageResponse;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.dto.RegisterRequest;
import com.aredondocharro.ClothingStore.shared.log.LogSanitizer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;



@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Auth", description = "User registration, verification, and authentication")
public class AuthController {

    private final RegisterUserUseCase registerUC;
    private final LoginUseCase loginUC;
    private final VerifyEmailUseCase verifyUC;
    private final DeleteUserUseCase deleteUserUC;
    private final RefreshCookieManager cookieManager; // Gestor de cookie HttpOnly para refresh
    private final TokenVerifierPort tokenVerifier;


    @Operation(
            summary = "Register a new user (email verification required)",
            description = "Creates the user with unverified email, sends a verification email, and returns HTTP 202 (Accepted).",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterRequest.class),
                            examples = @ExampleObject(
                                    value = "{ \"email\": \"user@example.com\", \"password\": \"Secret123!\", \"confirmPassword\": \"Secret123!\" }"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "202", description = "Registration accepted",
                            content = @Content(schema = @Schema(implementation = MessageResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Email already registered",
                            content = @Content(schema = @Schema(implementation = MessageResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error",
                            content = @Content(schema = @Schema(implementation = MessageResponse.class)))
            }
    )
    @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest req) {
        final IdentityEmail emailVO = IdentityEmail.of(req.getEmail()); // convertir String -> VO
        log.debug("POST /auth/register email={}", emailVO.getValue());

        registerUC.register(emailVO, req.getPassword(), req.getConfirmPassword());


        log.info("Registration accepted for email={}, pending verification", LogSanitizer.maskEmail(emailVO.getValue()));
        return ResponseEntity.accepted()
                .body(new MessageResponse("Check your email to verify your account."));
    }

    @Operation(
            summary = "Verify email (autologin)",
            description = "Validates the verification token and activates the account. If valid, issues tokens: " +
                    "sets refresh as HttpOnly cookie and returns access in response body.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Email verified",
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid or expired token",
                            content = @Content(schema = @Schema(implementation = MessageResponse.class)))
            }
    )
    @GetMapping("/verify")
    public ResponseEntity<AuthResponse> verify(@RequestParam("token") String token,
                                               HttpServletResponse res) {
        log.debug("GET /auth/verify");
        AuthResult result = verifyUC.verify(token);
        // Seteamos cookie HttpOnly con el refresh (persistido en BD)
        cookieManager.setCookie(res, result.refreshToken());
        log.info("Verification completed (autologin) -> refresh cookie set");
        // En el body devolvemos solo el access token
        return ResponseEntity.ok(new AuthResponse(result.accessToken(), null));
    }


    @Operation(
            summary = "Delete account (logout from all devices)",
            description = "Deletes the user account and all associated tokens, effectively logging out from all devices.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Account deleted successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized (invalid or missing token)",
                            content = @Content(schema = @Schema(implementation = MessageResponse.class))),
                    @ApiResponse(responseCode = "404", description = "User not found",
                            content = @Content(schema = @Schema(implementation = MessageResponse.class)))
            }
    )


    @DeleteMapping("/delete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> deleteAccount(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        String accessToken = authHeader.substring(7);

        // Verifica tu token ACCESS y obtén el userId del 'sub'
        var decoded = tokenVerifier.verify(accessToken, "access");

        // Borra al propietario del token (no aceptamos IDs externos)
        deleteUserUC.delete(decoded.userId());

        return ResponseEntity.ok(new MessageResponse("Account deleted successfully"));
    }


    @Operation(
            summary = "Login with email and password",
            description = "Authenticates user. Sets refresh as HttpOnly cookie and returns access in the response body.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Authenticated",
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials",
                            content = @Content(schema = @Schema(implementation = MessageResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Email not verified",
                            content = @Content(schema = @Schema(implementation = MessageResponse.class)))
            }
    )
    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req,
                                              HttpServletResponse res) {
        final IdentityEmail emailVO = IdentityEmail.of(req.email()); // convertir String -> VO (ajusta si tu DTO usa getters)
        log.debug("POST /auth/login email={}", emailVO.getValue());

        AuthResult result = loginUC.login(emailVO, req.password());

        // Seteamos cookie HttpOnly con el refresh (persistido en BD)
        log.info("Setting refresh cookie, len={}",
                result.refreshToken() != null ? result.refreshToken().length() : 0);
        cookieManager.setCookie(res, result.refreshToken());

        log.info("Login success email={}", LogSanitizer.maskEmail(emailVO.getValue()));
        // En el body devolvemos solo el access token
        return ResponseEntity.ok(new AuthResponse(result.accessToken(), null));
    }
}
