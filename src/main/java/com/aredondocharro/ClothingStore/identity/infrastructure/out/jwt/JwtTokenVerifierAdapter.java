package com.aredondocharro.ClothingStore.identity.infrastructure.out.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenVerifierPort;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
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
                .withClaim("type", expectedType)
                .build();

        DecodedJWT decoded = verifier.verify(token); // lanza si inválido/expirado
        UUID sub = UUID.fromString(decoded.getSubject());
        String jti = decoded.getId(); // <-- requiere que lo generes
        Instant exp = decoded.getExpiresAt().toInstant();

        log.debug("Verified {} token for userId={} jti={}", expectedType, sub, jti);
        return new DecodedToken(sub, jti, exp);
    }
}
