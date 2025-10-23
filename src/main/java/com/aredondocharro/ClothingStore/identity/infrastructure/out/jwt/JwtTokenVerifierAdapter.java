package com.aredondocharro.ClothingStore.identity.infrastructure.out.jwt;

import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenVerifierPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.error.TokenExpiredException;
import com.aredondocharro.ClothingStore.identity.domain.port.out.error.TokenInvalidException;
import com.aredondocharro.ClothingStore.identity.domain.port.out.error.TokenMissingClaimException;
import com.aredondocharro.ClothingStore.identity.domain.port.out.error.TokenUnsupportedTypeException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class JwtTokenVerifierAdapter implements TokenVerifierPort {

    private static final String CLAIM_TYPE  = "type";   // "access" | "refresh" | "verify"
    private static final String CLAIM_ROLES = "roles";  // ej. ["ROLE_ADMIN","ROLE_USER"]
    private static final String CLAIM_SCOPE = "scope";  // ej. "payments:charge cart:read"

    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public JwtTokenVerifierAdapter(String secret, String issuer) {
        this.algorithm = Algorithm.HMAC256(Objects.requireNonNull(secret, "secret is required"));
        this.verifier = JWT
                .require(algorithm)
                .withIssuer(Objects.requireNonNull(issuer, "issuer is required"))
                .acceptLeeway(2)
                .build();
    }

    @Override
    public DecodedToken verify(String token, String expectedType) {
        try {
            DecodedJWT jwt = verifier.verify(Objects.requireNonNull(token, "token is required"));

            // Tipo
            String actualType = jwt.getClaim(CLAIM_TYPE).asString();
            if (expectedType != null && !expectedType.equalsIgnoreCase(actualType)) {
                throw new TokenUnsupportedTypeException("Expected token type '" + expectedType + "' but was '" + actualType + "'");
            }

            // sub → UUID
            String sub = jwt.getSubject();
            if (sub == null || sub.isBlank()) throw new TokenMissingClaimException("Missing required claim: sub");
            UUID userUuid;
            try { userUuid = UUID.fromString(sub); }
            catch (IllegalArgumentException e) { throw new TokenMissingClaimException("Claim 'sub' must be a UUID string"); }

            // jti / iat / exp
            String jti = jwt.getId();
            Instant iat = toInstant(jwt.getIssuedAt());
            Instant exp = toInstant(jwt.getExpiresAt());

            // authorities: roles[] o scope (espacio-separado)
            List<String> authorities = extractAuthorities(jwt);

            // ⬇️ AHORA devolvemos los 5 campos
            return new DecodedToken(UserId.of(userUuid), jti, iat, exp, authorities);

        } catch (com.auth0.jwt.exceptions.TokenExpiredException e) {
            throw new TokenExpiredException("Token expired");
        } catch (SignatureVerificationException | AlgorithmMismatchException e) {
            throw new TokenInvalidException("Invalid signature/algorithm", e);
        } catch (InvalidClaimException e) {
            throw new TokenInvalidException("Invalid claim", e);
        } catch (JWTVerificationException e) {
            throw new TokenInvalidException("Invalid token", e);
        } catch (TokenUnsupportedTypeException | TokenMissingClaimException e) {
            throw e;
        } catch (Exception e) {
            throw new TokenInvalidException("Invalid token", e);
        }
    }

    private static Instant toInstant(Date d) {
        return d == null ? null : d.toInstant();
    }

    private static List<String> extractAuthorities(DecodedJWT jwt) {
        // 1) roles como lista
        try {
            List<String> roles = jwt.getClaim(CLAIM_ROLES).asList(String.class);
            if (roles != null) {
                return roles.stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toUnmodifiableList());
            }
        } catch (Exception ignored) { /* asList no disponible/claim distinto → seguimos */ }

        // 2) scope espacio-separado
        String scope = jwt.getClaim(CLAIM_SCOPE).asString();
        if (scope != null && !scope.isBlank()) {
            return Arrays.stream(scope.split("\\s+"))
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toUnmodifiableList());
        }

        // 3) vacío por defecto
        return List.of();
    }
}
