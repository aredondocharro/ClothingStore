// src/main/java/com/aredondocharro/ClothingStore/security/filter/AccessTokenFilter.java
package com.aredondocharro.ClothingStore.security.filter;

import com.aredondocharro.ClothingStore.security.port.AccessTokenVerifierPort;
import com.aredondocharro.ClothingStore.security.port.AuthPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

/**
 * Extrae y verifica el ACCESS token y establece Authentication en el contexto.
 * No corta la cadena si el token es inválido/ausente.
 */
public class AccessTokenFilter extends OncePerRequestFilter {

    private final AccessTokenVerifierPort verifier;

    public AccessTokenFilter(AccessTokenVerifierPort verifier) {
        this.verifier = Objects.requireNonNull(verifier, "verifier is required");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String raw = resolveAccessToken(request);

            if (raw != null && !raw.isBlank()) {
                try {
                    AuthPrincipal principal = verifier.verify(raw);

                    // Construimos las autoridades de Spring
                    java.util.List<SimpleGrantedAuthority> authorities =
                            principal.authorities().stream()
                                    .filter(Objects::nonNull)
                                    .map(SimpleGrantedAuthority::new)
                                    .toList();

                    // IMPORTANTE: el principal ahora es AuthPrincipal (no un String)
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    principal,                   // principal = AuthPrincipal
                                    null,                        // sin credenciales
                                    authorities                  // ya autenticado
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                } catch (AccessTokenVerifierPort.ExpiredTokenException |
                         AccessTokenVerifierPort.InvalidTokenException |
                         AccessTokenVerifierPort.MissingRequiredClaimException |
                         AccessTokenVerifierPort.UnsupportedTokenTypeException ex) {
                    // Dejamos la request sin Authentication; endpoints protegidos responderán 401/403.
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    /** Lee Authorization: Bearer <token>. */
    @Nullable
    private String resolveAccessToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7).trim();
        }
        return null;
    }
}
