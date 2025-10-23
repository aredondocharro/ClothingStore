package com.aredondocharro.ClothingStore.identity.infrastructure.out.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.aredondocharro.ClothingStore.identity.domain.model.Role;
import com.aredondocharro.ClothingStore.identity.domain.model.User;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenGeneratorPort;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

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
        Instant now = Instant.now();

        // Mapea Set<Role> -> String[]
        String[] roleNames = u.roles().stream()
                .map(Role::name)                // o .map(r -> "ROLE_" + r.name())
                .toArray(String[]::new);

        String token = JWT.create()
                .withIssuer(issuer)
                .withSubject(u.id().toString())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(accessSeconds)))
                .withClaim("email", u.email().getValue())
                .withArrayClaim("roles", roleNames)     // ← ahora sí: String[]
                .withClaim("type", "access")
                .sign(alg);

        log.debug("Access token generated for userId={}", u.id());
        return token;
    }

    @Override
    public String generateRefreshToken(User u) {
        Instant now = Instant.now();
        String token = JWT.create()
                .withIssuer(issuer)
                .withSubject(u.id().toString())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(refreshSeconds)))
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("type", "refresh")
                .sign(alg);

        log.debug("Refresh token generated for userId={}", u.id());
        return token;
    }

    @Override
    public String generateVerificationToken(User u) {
        Instant now = Instant.now();
        String token = JWT.create()
                .withIssuer(issuer)
                .withSubject(u.id().toString())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(verificationSeconds)))
                .withClaim("email", u.email().getValue())
                .withClaim("type", "verify")
                .sign(alg);

        log.debug("Verification token generated for userId={}", u.id());
        return token;
    }
}
