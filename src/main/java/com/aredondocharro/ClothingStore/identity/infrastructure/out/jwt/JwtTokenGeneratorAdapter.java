// src/main/java/com/aredondocharro/ClothingStore/identity/adapter/out/jwt/JwtTokenGeneratorAdapter.java
package com.aredondocharro.ClothingStore.identity.infrastructure.out.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.aredondocharro.ClothingStore.identity.domain.model.User;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenGeneratorPort;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Date;

@Slf4j
public class JwtTokenGeneratorAdapter implements TokenGeneratorPort {
    private final Algorithm alg;
    private final String issuer;
    private final long accessSeconds;
    private final long refreshSeconds;
    private final long verificationSeconds;

    public JwtTokenGeneratorAdapter(String secret, String issuer,
                                    long accessSeconds, long refreshSeconds, long verificationSeconds) {
        this.alg = Algorithm.HMAC256(secret);
        this.issuer = issuer;
        this.accessSeconds = accessSeconds;
        this.refreshSeconds = refreshSeconds;
        this.verificationSeconds = verificationSeconds;
    }

    @Override
    public String generateAccessToken(User u) {
        String token = JWT.create()
                .withIssuer(issuer)
                .withSubject(u.id().toString())
                .withClaim("email", u.email().getValue())
                .withArrayClaim("roles", u.roles().toArray(new String[0]))
                .withClaim("type", "access")
                .withExpiresAt(Date.from(Instant.now().plusSeconds(accessSeconds)))
                .sign(alg);
        log.debug("Access token generated for userId={}", u.id());
        return token;
    }

    @Override
    public String generateRefreshToken(User u) {
        String token = JWT.create()
                .withIssuer(issuer)
                .withSubject(u.id().toString())
                .withClaim("type", "refresh")
                .withExpiresAt(Date.from(Instant.now().plusSeconds(refreshSeconds)))
                .sign(alg);
        log.debug("Refresh token generated for userId={}", u.id());
        return token;
    }

    @Override
    public String generateVerificationToken(User u) {
        String token = JWT.create()
                .withIssuer(issuer)
                .withSubject(u.id().toString())
                .withClaim("email", u.email().getValue())
                .withClaim("type", "verify")
                .withExpiresAt(Date.from(Instant.now().plusSeconds(verificationSeconds)))
                .sign(alg);
        log.debug("Verification token generated for userId={}", u.id());
        return token;
    }
}
