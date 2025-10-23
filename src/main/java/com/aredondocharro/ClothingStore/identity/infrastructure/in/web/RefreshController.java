package com.aredondocharro.ClothingStore.identity.infrastructure.in.web;

import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.error.MissingRefreshCookieException;
import com.aredondocharro.ClothingStore.identity.domain.port.in.AuthResult;
import com.aredondocharro.ClothingStore.identity.domain.port.in.LogoutUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.in.RefreshAccessTokenUseCase;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.dto.AuthResponse;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.dto.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Auth", description = "Refresh and logout using an HttpOnly refresh cookie")
public class RefreshController {

    private final RefreshAccessTokenUseCase refreshUC;
    private final LogoutUseCase logoutUC;
    private final RefreshCookieManager cookieManager;

    private static final String REFRESH_COOKIE = "refresh_token";

    public RefreshController(RefreshAccessTokenUseCase refreshUC,
                             LogoutUseCase logoutUC,
                             RefreshCookieManager cookieManager) {
        this.refreshUC = refreshUC;
        this.logoutUC = logoutUC;
        this.cookieManager = cookieManager;
    }
    @Operation(
            summary = "Refresh Access Token (HttpOnly cookie)",
            description = "Usa la cookie HttpOnly 'refresh_token' enviada por el navegador; no hay body."
    )
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Parameter(hidden = true)
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshCookie,
            @Parameter(hidden = true) HttpServletRequest req,
            @Parameter(hidden = true) HttpServletResponse res
    ) {
        if (refreshCookie == null || refreshCookie.isBlank()) {
            // Devuelve tu error estándar (400/401). Usa tu excepción o un ResponseEntity.
            throw new MissingRefreshCookieException();
        }

        AuthResult result = refreshUC.refresh(
                refreshCookie,
                clientIp(req),
                req.getHeader("User-Agent")
        );

        // Rotación: setear la nueva refresh como HttpOnly cookie
        cookieManager.setCookie(res, result.refreshToken());

        // En el body solo el access; el refresh va en la cookie
        return ResponseEntity.ok(new AuthResponse(result.accessToken(), null));
    }

    @Operation(
            summary = "Logout (revoke current refresh)",
            description = """
        Revokes the refresh session associated with the HttpOnly cookie and clears the cookie.
        Use this to log out from the current device.
        """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Logged out",
                            content = @Content(schema = @Schema(implementation = MessageResponse.class)))
            }
    )
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(
            @Parameter(hidden = true)
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshCookie,
            @Parameter(hidden = true) HttpServletRequest req,
            @Parameter(hidden = true) HttpServletResponse res
    ) {
        if (refreshCookie != null && !refreshCookie.isBlank()) {
            logoutUC.logout(refreshCookie, clientIp(req));
        }
        cookieManager.clearCookie(res); // borra la cookie (Max-Age=0, Path=/auth, etc.)
        return ResponseEntity.ok(new MessageResponse("Logged out"));
    }

    private String clientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        return (xff != null && !xff.isBlank()) ? xff.split(",")[0].trim() : req.getRemoteAddr();
    }

}
