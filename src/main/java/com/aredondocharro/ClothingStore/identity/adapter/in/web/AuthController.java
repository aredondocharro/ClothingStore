package com.aredondocharro.ClothingStore.identity.adapter.in.web;

import com.aredondocharro.ClothingStore.identity.adapter.in.web.dto.*;
import com.aredondocharro.ClothingStore.identity.domain.port.in.LoginUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.in.RegisterUserUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.in.VerifyEmailUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @Operation(
            summary = "Register a new user (email verification required)",
            description = "Creates the user with unverified email, sends a verification email, and returns HTTP 202 (Accepted).",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterRequest.class),
                            examples = @ExampleObject(
                                    value = "{ \"email\": \"user@example.com\", \"password\": \"Secret123!\" }"
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
        log.debug("POST /auth/register email={}", req.email());
        registerUC.register(req.email(), req.password());
        log.info("Registration accepted for email={}", req.email());
        return ResponseEntity.accepted()
                .body(new MessageResponse("Check your email to verify your account."));
    }

    @Operation(
            summary = "Verify email (autologin)",
            description = "Validates the verification token and activates the account. If valid, returns JWT tokens.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Email verified",
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid or expired token",
                            content = @Content(schema = @Schema(implementation = MessageResponse.class)))
            }
    )
    @GetMapping("/verify")
    public ResponseEntity<AuthResponse> verify(@RequestParam("token") String token) {
        log.debug("GET /auth/verify");
        var result = verifyUC.verify(token);
        log.info("Verification completed (autologin)");
        return ResponseEntity.ok(new AuthResponse(result.accessToken(), result.refreshToken()));
    }

    @Operation(
            summary = "Login with email and password",
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
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        log.debug("POST /auth/login email={}", req.email());
        var result = loginUC.login(req.email(), req.password());
        log.info("Login success email={}", req.email());
        return ResponseEntity.ok(new AuthResponse(result.accessToken(), result.refreshToken()));
    }
}
