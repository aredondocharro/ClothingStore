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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Auth", description = "Refresh and logout using an HttpOnly refresh cookie")
@Slf4j
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
        final String ip = clientIp(req);
        final String ua = shortUa(req.getHeader("User-Agent"));
        final boolean cookiePresent = refreshCookie != null && !refreshCookie.isBlank();

        log.debug("Refresh request received (cookiePresent={}, ip={}, ua={})",
                cookiePresent, ip, ua);

        if (!cookiePresent) {
            log.warn("Refresh denied: missing '{}' cookie (ip={}, ua={})", REFRESH_COOKIE, ip, ua);
            throw new MissingRefreshCookieException();
        }

        // Do not log token contents
        AuthResult result = refreshUC.refresh(refreshCookie, ip, req.getHeader("User-Agent"));

        // Rotation: set the new refresh as an HttpOnly cookie
        cookieManager.setCookie(res, result.refreshToken());
        log.info("Refresh successful: access token issued and '{}' cookie rotated (ip={}, ua={})",
                REFRESH_COOKIE, ip, ua);

        // Body carries only the access token; refresh goes in the cookie
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
        final String ip = clientIp(req);
        final String ua = shortUa(req.getHeader("User-Agent"));
        final boolean cookiePresent = refreshCookie != null && !refreshCookie.isBlank();

        log.info("Logout request received (cookiePresent={}, ip={}, ua={})",
                cookiePresent, ip, ua);

        if (cookiePresent) {
            log.debug("Revoking refresh session associated with '{}' cookie (ip={}, ua={})",
                    REFRESH_COOKIE, ip, ua);
            logoutUC.logout(refreshCookie, ip);
        } else {
            log.debug("No session revoked: '{}' cookie not present (ip={}, ua={})", REFRESH_COOKIE, ip, ua);
        }

        cookieManager.clearCookie(res); // clears cookie (Max-Age=0, Path=/auth, etc.)
        log.info("'{}' cookie cleared from client (ip={}, ua={})", REFRESH_COOKIE, ip, ua);

        return ResponseEntity.ok(new MessageResponse("Logged out"));
    }

    private String clientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        String chosen = (xff != null && !xff.isBlank()) ? xff.split(",")[0].trim() : req.getRemoteAddr();
        if (log.isTraceEnabled()) {
            log.trace("Client IP resolution (chosen='{}', xff='{}', remote='{}')",
                    chosen, xff, req.getRemoteAddr());
        }
        return chosen;
    }

    private String shortUa(String ua) {
        if (ua == null || ua.isBlank()) return "unknown";
        // Truncate UA in logs to avoid excessive noise/PII
        return (ua.length() > 120) ? ua.substring(0, 120) + "…" : ua;
    }
}
