package com.aredondocharro.ClothingStore.identity.infrastructure.in.web;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshCookieManager {

    private final long refreshSeconds;
    private final boolean secureCookies;
    private final String sameSite;
    private final String path;
    private final String domain;

    private static final String REFRESH_COOKIE = "refresh_token";

    public RefreshCookieManager(
            @Value("${security.jwt.refresh.seconds}") long refreshSeconds,
            @Value("${security.cookies.secure:true}") boolean secureCookies,
            @Value("${security.cookies.sameSite:Strict}") String sameSite,
            @Value("${security.cookies.path:/auth}") String path,
            @Value("${security.cookies.domain:}") String domain
    ) {
        this.refreshSeconds = refreshSeconds;
        this.secureCookies = secureCookies;
        this.sameSite = sameSite;
        this.path = path;
        this.domain = domain;
    }

    public void setCookie(HttpServletResponse res, String refreshToken) {
        // 👈 Tipo correcto
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(REFRESH_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(secureCookies)
                .sameSite(sameSite)   // Strict / Lax / None (None requiere Secure+HTTPS)
                .path(path)
                .maxAge(refreshSeconds);

        if (domain != null && !domain.isBlank()) {
            builder = builder.domain(domain);
        }
        res.addHeader("Set-Cookie", builder.build().toString());
    }

    public void clearCookie(HttpServletResponse res) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(secureCookies)
                .sameSite(sameSite)
                .path(path)
                .maxAge(0);

        if (domain != null && !domain.isBlank()) {
            builder = builder.domain(domain);
        }
        res.addHeader("Set-Cookie", builder.build().toString());
    }
}
