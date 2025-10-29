package com.aredondocharro.ClothingStore.identity.infrastructure.in.web;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RefreshCookieManager {

    public static final String COOKIE_NAME = "refresh_token";

    private final Duration refreshTtl;
    private final boolean secureCookies;
    private final String sameSite; // Strict / Lax / None
    private final String path;     // p.ej. /auth
    private final String domain;   // vacío = host-only (recomendado)

    public RefreshCookieManager(
            @Value("${security.jwt.refresh.seconds}") long refreshSeconds,
            @Value("${security.cookies.secure:true}") boolean secureCookies,
            @Value("${security.cookies.sameSite:Strict}") String sameSite,
            @Value("${security.cookies.path:/auth}") String path,
            @Value("${security.cookies.domain:}") String domain
    ) {
        this.refreshTtl = Duration.ofSeconds(refreshSeconds);
        this.secureCookies = secureCookies;
        this.sameSite = sameSite;
        this.path = path;
        this.domain = domain;

        // --- Validaciones de arranque (fail-fast) ---
        if (refreshSeconds <= 0) {
            throw new IllegalArgumentException("security.jwt.refresh.seconds must be > 0");
        }
        if ("none".equalsIgnoreCase(sameSite) && !secureCookies) {
            throw new IllegalStateException("SameSite=None requires Secure=true (HTTPS).");
        }
    }

    public void setCookie(HttpServletResponse res, String refreshToken) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(secureCookies)
                .sameSite(sameSite)   // Strict / Lax / None
                .path(path)
                .maxAge(refreshTtl);

        if (domain != null && !domain.isBlank()) {
            builder = builder.domain(domain); // si lo defines, deja de ser host-only
        }
        res.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }

    public void clearCookie(HttpServletResponse res) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secureCookies)
                .sameSite(sameSite)
                .path(path)
                .maxAge(Duration.ZERO); // Max-Age=0

        if (domain != null && !domain.isBlank()) {
            builder = builder.domain(domain);
        }
        res.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }
}
