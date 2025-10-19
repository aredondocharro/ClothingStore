package com.aredondocharro.ClothingStore.identity.infrastructure.out.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenVerifierPort;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Slf4j
public class JwtTokenVerifierAdapter implements TokenVerifierPort {

    private final String issuer;
    private final Algorithm alg;

    public JwtTokenVerifierAdapter(String secret, String issuer) {
        this.issuer = issuer;
        this.alg = Algorithm.HMAC256(secret);
    }

    @Override
    public DecodedToken verify(String token, String expectedType) {
        JWTVerifier verifier = JWT.require(alg)
                .withIssuer(issuer)
                .withClaim("type", expectedType) // el generador debe poner claim "type"
                .acceptLeeway(3)                 // tolerancia de reloj opcional (segundos)
                .build();

        DecodedJWT jwt = verifier.verify(token); // lanza si firma/issuer/exp/type no válidos

        // sub → UUID → UserId (VO)
        String sub = jwt.getSubject();
        if (sub == null) throw new IllegalStateException("JWT missing 'sub'");
        UserId userId = UserId.of(UUID.fromString(sub));

        // jti obligatorio
        String jti = jwt.getId();
        if (jti == null || jti.isBlank()) throw new IllegalStateException("JWT missing 'jti'");

        // iat/exp
        Instant createdAt = dateToInstant(jwt.getIssuedAt());    // "iat"
        Instant expiresAt = dateToInstant(jwt.getExpiresAt());   // "exp"
        if (expiresAt == null) throw new IllegalStateException("JWT missing 'exp'");

        log.debug("Verified {} token for userId={} jti={} iat={} exp={}",
                expectedType, userId, jti, createdAt, expiresAt);

        return new DecodedToken(userId, jti, createdAt, expiresAt);
    }

    private static Instant dateToInstant(Date d) {
        return d != null ? d.toInstant() : null;
    }
}
