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
import java.util.List;
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
                AuthPrincipal principal = null;
                try {
                    principal = verifier.verify(raw); // puede devolver null o lanzar
                } catch (AccessTokenVerifierPort.ExpiredTokenException |
                         AccessTokenVerifierPort.InvalidTokenException |
                         AccessTokenVerifierPort.MissingRequiredClaimException |
                         AccessTokenVerifierPort.UnsupportedTokenTypeException ex) {
                    // Token inválido/expirado → no autenticamos y seguimos la cadena
                }

                if (principal != null) {
                    // Maneja authorities() null como lista vacía
                    List<SimpleGrantedAuthority> authorities =
                            (principal.authorities() == null ? List.<String>of() : principal.authorities())
                                    .stream()
                                    .filter(Objects::nonNull)
                                    .map(SimpleGrantedAuthority::new)
                                    .toList();

                    var authentication = new UsernamePasswordAuthenticationToken(
                            principal, null, authorities
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
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
