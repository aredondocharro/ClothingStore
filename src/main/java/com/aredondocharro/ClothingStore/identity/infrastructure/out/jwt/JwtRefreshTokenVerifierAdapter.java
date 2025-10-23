package com.aredondocharro.ClothingStore.identity.infrastructure.out.jwt;

import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.out.RefreshTokenVerifierPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.error.*;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Objects;

public class JwtRefreshTokenVerifierAdapter implements RefreshTokenVerifierPort {
    private final JWTVerifier verifier;

    public JwtRefreshTokenVerifierAdapter(String secret, String issuer) {
        this.verifier = JWT
                .require(Algorithm.HMAC256(Objects.requireNonNull(secret)))
                .withIssuer(Objects.requireNonNull(issuer))
                .withClaim("type", "refresh")
                .acceptLeeway(2)
                .build();
    }

    @Override
    public DecodedRefresh verify(String rawRefreshToken) {
        try {
            DecodedJWT jwt = verifier.verify(Objects.requireNonNull(rawRefreshToken));
            String sub = jwt.getSubject();
            String jti = jwt.getId();
            if (sub == null || jti == null) throw new TokenMissingClaimException("Missing sub/jti");

            return new DecodedRefresh(
                    UserId.of(java.util.UUID.fromString(sub)),
                    jti,
                    jwt.getIssuedAt().toInstant(),
                    jwt.getExpiresAt().toInstant()
            );
        } catch (com.auth0.jwt.exceptions.TokenExpiredException e) {
            throw new TokenExpiredException("Refresh expired");
        } catch (com.auth0.jwt.exceptions.InvalidClaimException e) {
            throw new TokenUnsupportedTypeException("Not a refresh token");
        } catch (com.auth0.jwt.exceptions.JWTVerificationException e) {
            throw new TokenInvalidException("Invalid refresh token: " + e.getMessage());
        }
    }
}
