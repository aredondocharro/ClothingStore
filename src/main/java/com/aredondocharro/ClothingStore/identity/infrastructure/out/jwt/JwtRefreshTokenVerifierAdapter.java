package com.aredondocharro.ClothingStore.identity.infrastructure.out.jwt;

import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.out.RefreshTokenVerifierPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.error.*;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class JwtRefreshTokenVerifierAdapter implements RefreshTokenVerifierPort {
    private final JWTVerifier verifier;

    public JwtRefreshTokenVerifierAdapter(String secret, String issuer) {
        Objects.requireNonNull(secret, "secret is required");
        Objects.requireNonNull(issuer, "issuer is required");

        this.verifier = JWT
                .require(Algorithm.HMAC256(secret))
                .withIssuer(issuer)
                .withClaim("type", "refresh")
                .acceptLeeway(2) // seconds
                .build();

        log.info("JWT Refresh verifier initialized (issuer='{}', leewaySeconds=2, claim 'type'='refresh')", issuer);
    }

    @Override
    public DecodedRefresh verify(String rawRefreshToken) {
        log.debug("Verifying refresh token (token not logged for security).");
        try {
            DecodedJWT jwt = verifier.verify(Objects.requireNonNull(rawRefreshToken, "token is required"));

            String sub = jwt.getSubject();
            String jti = jwt.getId();
            if (sub == null || jti == null) {
                log.warn("Refresh token missing required claims (sub and/or jti).");
                throw new TokenMissingClaimException("Missing sub/jti");
            }

            log.debug("Refresh token verified successfully (required claims present).");
            return new DecodedRefresh(
                    UserId.of(java.util.UUID.fromString(sub)),
                    jti,
                    jwt.getIssuedAt().toInstant(),
                    jwt.getExpiresAt().toInstant()
            );

        } catch (com.auth0.jwt.exceptions.TokenExpiredException e) {
            log.warn("Refresh token verification failed: token expired.");
            throw new TokenExpiredException("Refresh expired");
        } catch (com.auth0.jwt.exceptions.InvalidClaimException e) {
            // Usually means 'type' != 'refresh' (or wrong issuer, etc.)
            log.warn("Refresh token verification failed: invalid claim(s) (likely unsupported 'type'). Reason: {}", e.getMessage());
            throw new TokenUnsupportedTypeException("Not a refresh token");
        } catch (com.auth0.jwt.exceptions.JWTVerificationException e) {
            log.warn("Refresh token verification failed: {}", e.getMessage());
            throw new TokenInvalidException("Invalid refresh token: " + e.getMessage());
        }
    }
}
